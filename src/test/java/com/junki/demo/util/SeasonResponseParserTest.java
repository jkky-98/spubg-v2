package com.junki.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SeasonResponseParser 테스트")
class SeasonResponseParserTest {

    private ObjectMapper objectMapper;
    private JsonNode validSeasonResponse;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        
        String validJson = """
            {
                "data": [
                    {
                        "type": "season",
                        "id": "division.bro.official.pc-2018-01",
                        "attributes": {
                            "isCurrentSeason": false,
                            "isOffseason": false
                        }
                    },
                    {
                        "type": "season",
                        "id": "division.bro.official.pc-2024-01",
                        "attributes": {
                            "isCurrentSeason": true,
                            "isOffseason": false
                        }
                    },
                    {
                        "type": "season",
                        "id": "division.bro.official.pc-2024-offseason",
                        "attributes": {
                            "isCurrentSeason": false,
                            "isOffseason": true
                        }
                    }
                ],
                "links": {
                    "self": "string"
                },
                "meta": {}
            }
            """;
        
        validSeasonResponse = objectMapper.readTree(validJson);
    }

    @Test
    @DisplayName("시즌 목록 추출 성공")
    void extractSeasons_Success() {
        // when
        List<SeasonResponseParser.SeasonData> seasons = SeasonResponseParser.extractSeasons(validSeasonResponse);

        // then
        assertThat(seasons).hasSize(3);
        assertThat(seasons.get(0).seasonApiId()).isEqualTo("division.bro.official.pc-2018-01");
        assertThat(seasons.get(0).isCurrent()).isFalse();
        assertThat(seasons.get(1).seasonApiId()).isEqualTo("division.bro.official.pc-2024-01");
        assertThat(seasons.get(1).isCurrent()).isTrue();
        assertThat(seasons.get(2).seasonApiId()).isEqualTo("division.bro.official.pc-2024-offseason");
        assertThat(seasons.get(2).isOffseason()).isTrue();
    }

    @Test
    @DisplayName("현재 시즌 찾기 성공")
    void findCurrentSeason_Success() {
        // when
        Optional<SeasonResponseParser.SeasonData> currentSeason = SeasonResponseParser.findCurrentSeason(validSeasonResponse);

        // then
        assertThat(currentSeason).isPresent();
        assertThat(currentSeason.get().seasonApiId()).isEqualTo("division.bro.official.pc-2024-01");
        assertThat(currentSeason.get().isCurrent()).isTrue();
    }

    @Test
    @DisplayName("현재 시즌 찾기 실패 - 빈 응답")
    void findCurrentSeason_EmptyResponse() throws Exception {
        // given
        String emptyJson = "{}";
        JsonNode emptyResponse = objectMapper.readTree(emptyJson);

        // when
        Optional<SeasonResponseParser.SeasonData> currentSeason = SeasonResponseParser.findCurrentSeason(emptyResponse);

        // then
        assertThat(currentSeason).isEmpty();
    }
}

