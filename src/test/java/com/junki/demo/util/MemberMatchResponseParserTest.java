package com.junki.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MemberMatchResponseParser 테스트")
class MemberMatchResponseParserTest {

    private ObjectMapper objectMapper;
    private JsonNode validMemberResponse;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        
        // 여러 플레이어의 매치 목록이 포함된 응답
        String validJson = """
            {
                "data": [
                    {
                        "type": "player",
                        "id": "account.111",
                        "attributes": {
                            "name": "player1"
                        },
                        "relationships": {
                            "matches": {
                                "data": [
                                    {
                                        "type": "match",
                                        "id": "match-001"
                                    },
                                    {
                                        "type": "match",
                                        "id": "match-002"
                                    }
                                ]
                            }
                        }
                    },
                    {
                        "type": "player",
                        "id": "account.222",
                        "attributes": {
                            "name": "player2"
                        },
                        "relationships": {
                            "matches": {
                                "data": [
                                    {
                                        "type": "match",
                                        "id": "match-002"
                                    },
                                    {
                                        "type": "match",
                                        "id": "match-003"
                                    }
                                ]
                            }
                        }
                    }
                ]
            }
            """;
        
        validMemberResponse = objectMapper.readTree(validJson);
    }

    @Test
    @DisplayName("중복 제거된 match_id 추출 성공")
    void extractUniqueMatchIds_Success() {
        // when
        Set<String> matchIds = MemberMatchResponseParser.extractUniqueMatchIds(validMemberResponse);

        // then
        assertThat(matchIds).hasSize(3); // match-001, match-002, match-003 (중복 제거)
        assertThat(matchIds).containsExactlyInAnyOrder("match-001", "match-002", "match-003");
    }

    @Test
    @DisplayName("플레이어별 매치 목록 추출 성공")
    void extractPlayerMatches_Success() {
        // when
        List<MemberMatchResponseParser.PlayerMatchData> playerMatches = 
                MemberMatchResponseParser.extractPlayerMatches(validMemberResponse);

        // then
        assertThat(playerMatches).hasSize(2);
        assertThat(playerMatches.get(0).playerId()).isEqualTo("account.111");
        assertThat(playerMatches.get(0).matchIds()).containsExactlyInAnyOrder("match-001", "match-002");
        assertThat(playerMatches.get(1).playerId()).isEqualTo("account.222");
        assertThat(playerMatches.get(1).matchIds()).containsExactlyInAnyOrder("match-002", "match-003");
    }

    @Test
    @DisplayName("빈 응답 처리")
    void extractUniqueMatchIds_EmptyResponse() throws Exception {
        // given
        String emptyJson = "{}";
        JsonNode emptyResponse = objectMapper.readTree(emptyJson);

        // when
        Set<String> matchIds = MemberMatchResponseParser.extractUniqueMatchIds(emptyResponse);

        // then
        assertThat(matchIds).isEmpty();
    }
}

