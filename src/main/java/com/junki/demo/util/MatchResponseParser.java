package com.junki.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.junki.demo.enums.GameMap;
import com.junki.demo.enums.GameMode;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * PUBG API Match 응답 파싱 유틸리티
 */
@Slf4j
public class MatchResponseParser {

    /**
     * Match 응답에서 asset_id 추출
     * 경로: data.relationships.assets.data[0].id
     */
    public static Optional<String> extractAssetId(JsonNode rootNode) {
        JsonNode assetsArray = rootNode.path("data")
                .path("relationships")
                .path("assets")
                .path("data");

        if (assetsArray.isArray() && assetsArray.size() > 0) {
            JsonNode firstAsset = assetsArray.get(0);
            if ("asset".equals(firstAsset.path("type").asText())) {
                String assetId = firstAsset.path("id").asText();
                if (!assetId.isEmpty()) {
                    return Optional.of(assetId);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Match 응답에서 asset_url 추출
     * included 배열에서 type이 "asset"이고 id가 일치하는 항목의 attributes.URL
     */
    public static Optional<String> extractAssetUrl(JsonNode rootNode, String assetId) {
        JsonNode included = rootNode.path("included");
        
        if (included.isArray()) {
            for (JsonNode node : included) {
                if ("asset".equals(node.path("type").asText()) 
                        && assetId.equals(node.path("id").asText())) {
                    String assetUrl = node.path("attributes").path("URL").asText();
                    if (!assetUrl.isEmpty()) {
                        return Optional.of(assetUrl);
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Match 응답에서 mapName 추출
     * 경로: data.attributes.mapName
     */
    public static Optional<String> extractMapName(JsonNode rootNode) {
        String mapName = rootNode.path("data")
                .path("attributes")
                .path("mapName")
                .asText(null);
        
        return mapName != null && !mapName.isEmpty() 
                ? Optional.of(mapName) 
                : Optional.empty();
    }

    /**
     * Match 응답에서 gameMode 추출
     * 경로: data.attributes.gameMode
     */
    public static Optional<String> extractGameMode(JsonNode rootNode) {
        String gameMode = rootNode.path("data")
                .path("attributes")
                .path("gameMode")
                .asText(null);
        
        return gameMode != null && !gameMode.isEmpty() 
                ? Optional.of(gameMode) 
                : Optional.empty();
    }

    /**
     * Match 응답에서 matchId 추출
     * 경로: data.id
     */
    public static Optional<String> extractMatchId(JsonNode rootNode) {
        String matchId = rootNode.path("data")
                .path("id")
                .asText(null);
        
        return matchId != null && !matchId.isEmpty() 
                ? Optional.of(matchId) 
                : Optional.empty();
    }

    /**
     * Match 응답에서 GameMap enum 추출
     */
    public static Optional<GameMap> extractGameMap(JsonNode rootNode) {
        return extractMapName(rootNode)
                .map(GameMap::fromString);
    }

    /**
     * Match 응답에서 GameMode enum 추출
     */
    public static Optional<GameMode> extractGameModeEnum(JsonNode rootNode) {
        return extractGameMode(rootNode)
                .map(mode -> {
                    try {
                        return GameMode.valueOf(mode.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("[MatchResponseParser] 알 수 없는 gameMode: {}", mode);
                        return GameMode.OTHER;
                    }
                });
    }
}

