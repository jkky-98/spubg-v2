package com.junki.demo.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.junki.demo.entity.Match;
import com.junki.demo.entity.Member;
import com.junki.demo.message.MatchProcessMessage;
import com.junki.demo.repository.MatchRepository;
import com.junki.demo.repository.MemberRepository;
import com.junki.demo.service.PubgApiManager;
import com.junki.demo.service.SeasonService;
import com.junki.demo.util.MemberMatchResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.junki.demo.config.RabbitMQConfig.MATCH_PROCESS_QUEUE;

/**
 * 매치 동기화 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchSyncScheduler {
    private final MemberRepository memberRepository;
    private final MatchRepository matchRepository;
    private final PubgApiManager pubgApiManager;
    private final SeasonService seasonService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 시즌 동기화 (매일 00시 실행)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncSeasons() {
        log.info("[MatchSyncScheduler][syncSeasons] 시즌 동기화 스케줄 실행");
        try {
            seasonService.syncSeasons();
            log.info("[MatchSyncScheduler][syncSeasons] 시즌 동기화 완료");
        } catch (Exception e) {
            log.error("[MatchSyncScheduler][syncSeasons] 시즌 동기화 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 매치 수집 및 RabbitMQ 발행 (매일 00시 30분 실행)
     */
    @Scheduled(cron = "0 30 0 * * ?")
    @Transactional(readOnly = true)
    public void collectMatches() {
        log.info("[MatchSyncScheduler][collectMatches] 매치 수집 스케줄 실행");

        try {
            // PUBG 계정이 연동된 멤버만 조회
            List<Member> linkedMembers = memberRepository.findAll().stream()
                    .filter(Member::isLinked)
                    .toList();

            if (linkedMembers.isEmpty()) {
                log.warn("[MatchSyncScheduler][collectMatches] 연동된 멤버가 없습니다");
                return;
            }

            log.info("[MatchSyncScheduler][collectMatches] 연동된 멤버 수: {}", linkedMembers.size());

            // 멤버 이름 리스트 추출
            List<String> usernames = linkedMembers.stream()
                    .map(Member::getUsername)
                    .toList();

            // 여러 멤버 한 번에 조회
            JsonNode response = pubgApiManager.requestManyMembers(usernames);

            // 중복 제거된 match_id 추출
            Set<String> uniqueMatchIds = MemberMatchResponseParser.extractUniqueMatchIds(response);

            log.info("[MatchSyncScheduler][collectMatches] 수집된 매치 수: {}", uniqueMatchIds.size());

            if (uniqueMatchIds.isEmpty()) {
                log.info("[MatchSyncScheduler][collectMatches] 수집된 매치가 없습니다");
                return;
            }

            // 이미 존재하는 매치 제외
            Set<String> existingMatchIds = matchRepository.findByMatchApiIdIn(uniqueMatchIds)
                    .stream()
                    .map(Match::getMatchApiId)
                    .collect(Collectors.toSet());

            List<String> newMatchIds = uniqueMatchIds.stream()
                    .filter(id -> !existingMatchIds.contains(id))
                    .toList();

            log.info("[MatchSyncScheduler][collectMatches] 신규 매치: {}, 기존 매치: {}", 
                    newMatchIds.size(), existingMatchIds.size());

            if (newMatchIds.isEmpty()) {
                log.info("[MatchSyncScheduler][collectMatches] 모든 매치가 이미 존재합니다");
                return;
            }

            // 현재 시즌 조회
            String seasonApiId = seasonService.getCurrentSeason().getSeasonApiId();

            // RabbitMQ에 메시지 발행
            for (String matchId : newMatchIds) {
                // 메시지 구조를 단일 ID를 받도록 변경하거나, List.of(matchId) 사용
                MatchProcessMessage message = new MatchProcessMessage(matchId, seasonApiId);
                rabbitTemplate.convertAndSend(MATCH_PROCESS_QUEUE, message);
            }

            log.info("[MatchSyncScheduler][collectMatches] 매치 수집 및 발행 완료");
        } catch (Exception e) {
            log.error("[MatchSyncScheduler][collectMatches] 매치 수집 실패: {}", e.getMessage(), e);
        }
    }
}

