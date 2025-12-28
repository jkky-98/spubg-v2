package com.junki.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junki.demo.enums.GameMap;
import com.junki.demo.enums.GameMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MatchResponseParser 테스트")
class MatchResponseParserTest {

    private ObjectMapper objectMapper;
    private JsonNode validMatchResponse;
    private JsonNode emptyMatchResponse;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        
        // 실제 PUBG API 응답 구조를 기반으로 한 테스트 데이터
        String validJson = """
            {
                "data": {
                    "type": "match",
                    "id": "29c0c3c1-3b83-4e90-a29a-c0721efa014a",
                    "attributes": {
                        "isCustomMatch": false,
                        "matchType": "official",
                        "seasonState": "progress",
                        "titleId": "bluehole-pubg",
                        "mapName": "Tiger_Main",
                        "stats": null,
                        "gameMode": "squad",
                        "shardId": "steam",
                        "tags": null,
                        "createdAt": "2025-12-13T04:16:16Z",
                        "duration": 1451
                    },
                    "relationships": {
                        "rosters": {
                            "data": []
                        },
                        "assets": {
                            "data": [
                                {
                                    "type": "asset",
                                    "id": "020a8739-d7de-11f0-805a-9e69fe1c61d1"
                                }
                            ]
                        }
                    },
                    "links": {
                        "self": "https://api.pubg.com/shards/steam/matches/29c0c3c1-3b83-4e90-a29a-c0721efa014a",
                        "schema": ""
                    }
                },
                "included": [
                    {
                        "type": "asset",
                        "id": "020a8739-d7de-11f0-805a-9e69fe1c61d1",
                        "attributes": {
                            "URL": "https://telemetry-cdn.pubg.com/telemetry/020a8739-d7de-11f0-805a-9e69fe1c61d1.json",
                            "name": "telemetry",
                            "description": ""
                        }
                    }
                ]
            }
            """;
        
        validMatchResponse = objectMapper.readTree(validJson);
        
        // 빈 응답 테스트용
        String emptyJson = "{}";
        emptyMatchResponse = objectMapper.readTree(emptyJson);
    }

    @Test
    @DisplayName("asset_id 추출 성공")
    void extractAssetId_Success() {
        // when
        Optional<String> assetId = MatchResponseParser.extractAssetId(validMatchResponse);

        // then
        assertThat(assetId).isPresent();
        assertThat(assetId.get()).isEqualTo("020a8739-d7de-11f0-805a-9e69fe1c61d1");
    }

    @Test
    @DisplayName("asset_id 추출 실패 - 빈 응답")
    void extractAssetId_EmptyResponse() {
        // when
        Optional<String> assetId = MatchResponseParser.extractAssetId(emptyMatchResponse);

        // then
        assertThat(assetId).isEmpty();
    }

    @Test
    @DisplayName("asset_id 추출 실패 - assets 배열이 비어있음")
    void extractAssetId_EmptyAssetsArray() throws Exception {
        // given
        String json = """
            {
                "data": {
                    "relationships": {
                        "assets": {
                            "data": []
                        }
                    }
                }
            }
            """;
        JsonNode response = objectMapper.readTree(json);

        // when
        Optional<String> assetId = MatchResponseParser.extractAssetId(response);

        // then
        assertThat(assetId).isEmpty();
    }

    @Test
    @DisplayName("asset_url 추출 성공")
    void extractAssetUrl_Success() {
        // given
        String assetId = "020a8739-d7de-11f0-805a-9e69fe1c61d1";

        // when
        Optional<String> assetUrl = MatchResponseParser.extractAssetUrl(validMatchResponse, assetId);

        // then
        assertThat(assetUrl).isPresent();
        assertThat(assetUrl.get()).isEqualTo("https://telemetry-cdn.pubg.com/telemetry/020a8739-d7de-11f0-805a-9e69fe1c61d1.json");
    }

    @Test
    @DisplayName("asset_url 추출 실패 - 일치하는 assetId 없음")
    void extractAssetUrl_NotFound() {
        // given
        String wrongAssetId = "wrong-asset-id";

        // when
        Optional<String> assetUrl = MatchResponseParser.extractAssetUrl(validMatchResponse, wrongAssetId);

        // then
        assertThat(assetUrl).isEmpty();
    }

    @Test
    @DisplayName("asset_url 추출 실패 - 빈 응답")
    void extractAssetUrl_EmptyResponse() {
        // when
        Optional<String> assetUrl = MatchResponseParser.extractAssetUrl(emptyMatchResponse, "some-id");

        // then
        assertThat(assetUrl).isEmpty();
    }

    @Test
    @DisplayName("mapName 추출 성공")
    void extractMapName_Success() {
        // when
        Optional<String> mapName = MatchResponseParser.extractMapName(validMatchResponse);

        // then
        assertThat(mapName).isPresent();
        assertThat(mapName.get()).isEqualTo("Tiger_Main");
    }

    @Test
    @DisplayName("mapName 추출 실패 - 빈 응답")
    void extractMapName_EmptyResponse() {
        // when
        Optional<String> mapName = MatchResponseParser.extractMapName(emptyMatchResponse);

        // then
        assertThat(mapName).isEmpty();
    }

    @Test
    @DisplayName("gameMode 추출 성공")
    void extractGameMode_Success() {
        // when
        Optional<String> gameMode = MatchResponseParser.extractGameMode(validMatchResponse);

        // then
        assertThat(gameMode).isPresent();
        assertThat(gameMode.get()).isEqualTo("squad");
    }

    @Test
    @DisplayName("gameMode 추출 실패 - 빈 응답")
    void extractGameMode_EmptyResponse() {
        // when
        Optional<String> gameMode = MatchResponseParser.extractGameMode(emptyMatchResponse);

        // then
        assertThat(gameMode).isEmpty();
    }

    @Test
    @DisplayName("matchId 추출 성공")
    void extractMatchId_Success() {
        // when
        Optional<String> matchId = MatchResponseParser.extractMatchId(validMatchResponse);

        // then
        assertThat(matchId).isPresent();
        assertThat(matchId.get()).isEqualTo("29c0c3c1-3b83-4e90-a29a-c0721efa014a");
    }

    @Test
    @DisplayName("matchId 추출 실패 - 빈 응답")
    void extractMatchId_EmptyResponse() {
        // when
        Optional<String> matchId = MatchResponseParser.extractMatchId(emptyMatchResponse);

        // then
        assertThat(matchId).isEmpty();
    }

    @Test
    @DisplayName("GameMap enum 추출 성공 - Tiger_Main")
    void extractGameMap_Success() {
        // when
        Optional<GameMap> gameMap = MatchResponseParser.extractGameMap(validMatchResponse);

        // then
        assertThat(gameMap).isPresent();
        assertThat(gameMap.get()).isEqualTo(GameMap.TIGER_MAIN);
    }

    @Test
    @DisplayName("GameMap enum 추출 성공 - Erangel_Main")
    void extractGameMap_Erangel() throws Exception {
        // given
        String json = """
            {
                "data": {
                    "attributes": {
                        "mapName": "Erangel_Main"
                    }
                }
            }
            """;
        JsonNode response = objectMapper.readTree(json);

        // when
        Optional<GameMap> gameMap = MatchResponseParser.extractGameMap(response);

        // then
        assertThat(gameMap).isPresent();
        assertThat(gameMap.get()).isEqualTo(GameMap.ERANGEL_MAIN);
    }

    @Test
    @DisplayName("GameMap enum 추출 실패 - 알 수 없는 맵")
    void extractGameMap_Unknown() throws Exception {
        // given
        String json = """
            {
                "data": {
                    "attributes": {
                        "mapName": "Unknown_Map"
                    }
                }
            }
            """;
        JsonNode response = objectMapper.readTree(json);

        // when
        Optional<GameMap> gameMap = MatchResponseParser.extractGameMap(response);

        // then
        assertThat(gameMap).isPresent();
        assertThat(gameMap.get()).isEqualTo(GameMap.UNKNOWN);
    }

    @Test
    @DisplayName("GameMap enum 추출 실패 - 빈 응답")
    void extractGameMap_EmptyResponse() {
        // when
        Optional<GameMap> gameMap = MatchResponseParser.extractGameMap(emptyMatchResponse);

        // then
        assertThat(gameMap).isEmpty();
    }

    @Test
    @DisplayName("GameMode enum 추출 성공 - squad")
    void extractGameModeEnum_Squad() {
        // when
        Optional<GameMode> gameMode = MatchResponseParser.extractGameModeEnum(validMatchResponse);

        // then
        assertThat(gameMode).isPresent();
        assertThat(gameMode.get()).isEqualTo(GameMode.SQUAD);
    }

    @Test
    @DisplayName("GameMode enum 추출 성공 - duo")
    void extractGameModeEnum_Duo() throws Exception {
        // given
        String json = """
            {
                "data": {
                    "attributes": {
                        "gameMode": "duo"
                    }
                }
            }
            """;
        JsonNode response = objectMapper.readTree(json);

        // when
        Optional<GameMode> gameMode = MatchResponseParser.extractGameModeEnum(response);

        // then
        assertThat(gameMode).isPresent();
        assertThat(gameMode.get()).isEqualTo(GameMode.DUO);
    }

    @Test
    @DisplayName("GameMode enum 추출 실패 - 알 수 없는 모드")
    void extractGameModeEnum_Unknown() throws Exception {
        // given
        String json = """
            {
                "data": {
                    "attributes": {
                        "gameMode": "unknown_mode"
                    }
                }
            }
            """;
        JsonNode response = objectMapper.readTree(json);

        // when
        Optional<GameMode> gameMode = MatchResponseParser.extractGameModeEnum(response);

        // then
        assertThat(gameMode).isPresent();
        assertThat(gameMode.get()).isEqualTo(GameMode.OTHER);
    }

    @Test
    @DisplayName("GameMode enum 추출 실패 - 빈 응답")
    void extractGameModeEnum_EmptyResponse() {
        // when
        Optional<GameMode> gameMode = MatchResponseParser.extractGameModeEnum(emptyMatchResponse);

        // then
        assertThat(gameMode).isEmpty();
    }

    @Test
    @DisplayName("전체 파싱 통합 테스트")
    void parseCompleteMatchResponse() {
        // when
        Optional<String> matchId = MatchResponseParser.extractMatchId(validMatchResponse);
        Optional<String> assetId = MatchResponseParser.extractAssetId(validMatchResponse);
        Optional<String> assetUrl = MatchResponseParser.extractAssetUrl(validMatchResponse, assetId.orElse(""));
        Optional<GameMap> gameMap = MatchResponseParser.extractGameMap(validMatchResponse);
        Optional<GameMode> gameMode = MatchResponseParser.extractGameModeEnum(validMatchResponse);

        // then
        assertThat(matchId).isPresent();
        assertThat(assetId).isPresent();
        assertThat(assetUrl).isPresent();
        assertThat(gameMap).isPresent();
        assertThat(gameMode).isPresent();

        assertThat(matchId.get()).isEqualTo("29c0c3c1-3b83-4e90-a29a-c0721efa014a");
        assertThat(assetId.get()).isEqualTo("020a8739-d7de-11f0-805a-9e69fe1c61d1");
        assertThat(assetUrl.get()).contains("telemetry");
        assertThat(gameMap.get()).isEqualTo(GameMap.TIGER_MAIN);
        assertThat(gameMode.get()).isEqualTo(GameMode.SQUAD);
    }
}

