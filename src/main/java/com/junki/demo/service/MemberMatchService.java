package com.junki.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.junki.demo.entity.Match;
import com.junki.demo.entity.Member;
import com.junki.demo.entity.MemberMatch;
import com.junki.demo.repository.MatchRepository;
import com.junki.demo.repository.MemberMatchRepository;
import com.junki.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberMatchService {

    private final MatchRepository matchRepository;
    private final MemberRepository memberRepository;
    private final MemberMatchRepository memberMatchRepository;
    private final PubgApiManager pubgApiManager;

    public void analyzeMatch(Long matchId, String matchApiId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("매치를 찾을 수 없습니다. ID: " + matchId));

        if (match.isAnalysis()) return; // 이미 분석됨

        // 1. 매치 상세 데이터 가져오기
        JsonNode matchData = pubgApiManager.requestMatch(matchApiId);

        // 2. 현재 연동된 모든 멤버의 accountId를 가져와서 Map 생성 (비교 최적화)
        List<Member> members = memberRepository.findAll().stream()
                .filter(Member::isLinked)
                .toList();

        // 3. included 배열 순회하며 participant 찾기
        JsonNode included = matchData.path("included");
        for (JsonNode node : included) {
            if ("participant".equals(node.path("type").asText())) {
                String playerId = node.path("attributes").path("stats").path("playerId").asText();

                // 우리 멤버인지 확인
                members.stream()
                        .filter(m -> m.getAccountId().equals(playerId))
                        .findFirst()
                        .ifPresent(member -> {
                            saveMemberMatch(member, match);
                        });
            }
        }

        // 4. 분석 완료 처리
        match.finishAnalysis();
    }

    private void saveMemberMatch(Member member, Match match) {
        // 중복 저장 방지
        if (!memberMatchRepository.existsByMemberAndMatch(member, match)) {
            MemberMatch memberMatch = MemberMatch.builder()
                    .member(member)
                    .match(match)
                    .build();
            memberMatchRepository.save(memberMatch);
            log.info("[MatchAnalysisService] 멤버-매치 연결 완료: {} <-> {}",
                    member.getUsername(), match.getMatchApiId());
        }
    }
}
