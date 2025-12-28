package com.junki.demo.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PUBG API Season 응답 파싱 유틸리티
 */
public class SeasonResponseParser {

    /**
     * Season 응답에서 모든 시즌 정보 추출
     */
    public static List<SeasonData> extractSeasons(JsonNode rootNode) {
        List<SeasonData> seasons = new ArrayList<>();
        JsonNode dataArray = rootNode.path("data");

        if (dataArray.isArray()) {
            for (JsonNode seasonNode : dataArray) {
                String seasonApiId = seasonNode.path("id").asText(null);
                JsonNode attributes = seasonNode.path("attributes");
                boolean isCurrent = attributes.path("isCurrentSeason").asBoolean(false);
                boolean isOffseason = attributes.path("isOffseason").asBoolean(false);

                if (seasonApiId != null && !seasonApiId.isEmpty()) {
                    seasons.add(new SeasonData(seasonApiId, isCurrent, isOffseason));
                }
            }
        }

        return seasons;
    }

    /**
     * 현재 시즌 찾기
     */
    public static Optional<SeasonData> findCurrentSeason(JsonNode rootNode) {
        return extractSeasons(rootNode).stream()
                .filter(SeasonData::isCurrent)
                .findFirst();
    }

    /**
     * Season 데이터 DTO
     */
    public record SeasonData(
            String seasonApiId,
            boolean isCurrent,
            boolean isOffseason
    ) {}
}

