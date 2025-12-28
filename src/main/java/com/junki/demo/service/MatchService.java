package com.junki.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.junki.demo.entity.Match;
import com.junki.demo.entity.Season;
import com.junki.demo.enums.GameMap;
import com.junki.demo.enums.GameMode;
import com.junki.demo.repository.MatchRepository;
import com.junki.demo.repository.SeasonRepository;
import com.junki.demo.util.MatchResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchService {
    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final PubgApiManager pubgApiManager;

    /**
     * 단일 매치 상세 처리 및 저장 (컨슈머에서 호출)
     */
    public void processMatch(String matchApiId, String seasonApiId) {
        log.info("[MatchService][processMatch] 단일 매치 처리 시작: {}", matchApiId);

        // 1. 중복 체크 (안전장치: 큐에 들어가는 사이 이미 처리됐을 수도 있음)
        if (matchRepository.existsByMatchApiId(matchApiId)) {
            log.info("[MatchService][processMatch] 이미 존재하는 매치입니다: {}", matchApiId);
            return;
        }

        // 2. 시즌 조회
        Season season = seasonRepository.findBySeasonApiId(seasonApiId)
                .orElseThrow(() -> new IllegalStateException("시즌을 찾을 수 없습니다: " + seasonApiId));

        try {
            // 3. API 호출
            JsonNode matchResponse = pubgApiManager.requestMatch(matchApiId);

            // 4. 시즌 상태 확인 (커스텀 요구사항)
            String seasonState = matchResponse.path("data")
                    .path("attributes")
                    .path("seasonState")
                    .asText(null);

            if (!"progress".equals(seasonState)) {
                log.info("[MatchService][processMatch] progress 상태가 아닌 매치입니다. 건너뜁니다.");
                return;
            }

            // 5. 데이터 파싱 및 엔티티 생성
            String assetId = MatchResponseParser.extractAssetId(matchResponse)
                    .orElseThrow(() -> new IllegalStateException("assetId 추출 실패"));

            String assetUrl = MatchResponseParser.extractAssetUrl(matchResponse, assetId)
                    .orElseThrow(() -> new IllegalStateException("assetUrl 추출 실패"));

            GameMap gameMap = MatchResponseParser.extractGameMap(matchResponse).orElse(GameMap.UNKNOWN);
            GameMode gameMode = MatchResponseParser.extractGameModeEnum(matchResponse).orElse(GameMode.OTHER);

            Match match = Match.builder()
                    .matchApiId(matchApiId)
                    .assetId(assetId)
                    .assetUrl(assetUrl)
                    .map(gameMap)
                    .gameMode(gameMode)
                    .season(season)
                    .analysis(false)
                    .build();

            // 6. DB 저장
            matchRepository.save(match);
            log.info("[MatchService][processMatch] 매치 저장 완료: {}", matchApiId);

        } catch (Exception e) {
            log.error("[MatchService][processMatch] 매치 처리 중 예외 발생: {}", matchApiId, e);
            throw e; // 컨슈머가 재시도할 수 있도록 예외 전파
        }
    }
}