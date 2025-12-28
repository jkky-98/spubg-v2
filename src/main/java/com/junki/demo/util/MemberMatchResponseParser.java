package com.junki.demo.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PUBG API Member Match 응답 파싱 유틸리티
 * 플레이어 정보 조회 시 반환되는 match 목록을 파싱
 */
public class MemberMatchResponseParser {

    /**
     * 여러 플레이어의 match 목록에서 중복 제거된 match_id 추출
     * 응답 구조: {"data": [{"id": "account.xxx", "relationships": {"matches": {"data": [...]}}}]}
     */
    public static Set<String> extractUniqueMatchIds(JsonNode rootNode) {
        Set<String> matchIds = new HashSet<>();
        JsonNode dataArray = rootNode.path("data");

        if (dataArray.isArray()) {
            for (JsonNode playerNode : dataArray) {
                JsonNode matchesArray = playerNode.path("relationships")
                        .path("matches")
                        .path("data");

                if (matchesArray.isArray()) {
                    for (JsonNode matchNode : matchesArray) {
                        if ("match".equals(matchNode.path("type").asText())) {
                            String matchId = matchNode.path("id").asText();
                            if (matchId != null && !matchId.isEmpty()) {
                                matchIds.add(matchId);
                            }
                        }
                    }
                }
            }
        }

        return matchIds;
    }

    /**
     * 각 플레이어별 match 목록 추출 (플레이어 ID와 매칭)
     */
    public static List<PlayerMatchData> extractPlayerMatches(JsonNode rootNode) {
        List<PlayerMatchData> playerMatches = new ArrayList<>();
        JsonNode dataArray = rootNode.path("data");

        if (dataArray.isArray()) {
            for (JsonNode playerNode : dataArray) {
                String playerId = playerNode.path("id").asText(null);
                Set<String> matchIds = new HashSet<>();

                JsonNode matchesArray = playerNode.path("relationships")
                        .path("matches")
                        .path("data");

                if (matchesArray.isArray()) {
                    for (JsonNode matchNode : matchesArray) {
                        if ("match".equals(matchNode.path("type").asText())) {
                            String matchId = matchNode.path("id").asText();
                            if (matchId != null && !matchId.isEmpty()) {
                                matchIds.add(matchId);
                            }
                        }
                    }
                }

                if (playerId != null && !playerId.isEmpty() && !matchIds.isEmpty()) {
                    playerMatches.add(new PlayerMatchData(playerId, matchIds));
                }
            }
        }

        return playerMatches;
    }

    /**
     * 플레이어와 매치 매핑 데이터
     */
    public record PlayerMatchData(
            String playerId,
            Set<String> matchIds
    ) {}
}

