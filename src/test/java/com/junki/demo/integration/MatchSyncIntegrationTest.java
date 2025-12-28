package com.junki.demo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junki.demo.entity.Member;
import com.junki.demo.entity.Season;
import com.junki.demo.repository.MemberRepository;
import com.junki.demo.repository.MatchRepository;
import com.junki.demo.repository.SeasonRepository;
import com.junki.demo.service.MatchService;
import com.junki.demo.service.PubgApiManager;
import com.junki.demo.service.SeasonService;
import com.junki.demo.util.MatchResponseParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("매치 동기화 통합 테스트")
@Transactional
class MatchSyncIntegrationTest {

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private MatchService matchService;

    @MockitoBean
    private PubgApiManager pubgApiManager;

    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestRabbitMQConfig {
        @Bean
        @Primary
        public ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }

        @Bean
        @Primary
        public RabbitTemplate rabbitTemplate() {
            return mock(RabbitTemplate.class);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("시즌 동기화 및 매치 처리 통합 테스트")
    void seasonSyncAndMatchProcess() throws Exception {
        // given - 시즌 응답
        String seasonJson = """
            {
                "data": [
                    {
                        "type": "season",
                        "id": "division.bro.official.pc-2024-01",
                        "attributes": {
                            "isCurrentSeason": true,
                            "isOffseason": false
                        }
                    }
                ]
            }
            """;
        JsonNode seasonResponse = objectMapper.readTree(seasonJson);
        when(pubgApiManager.requestSeasons()).thenReturn(seasonResponse);

        // when - 시즌 동기화
        seasonService.syncSeasons();

        // then
        Season currentSeason = seasonService.getCurrentSeason();
        assertThat(currentSeason).isNotNull();
        assertThat(currentSeason.isCurrent()).isTrue();

        // given - 매치 응답
        String matchJson = """
            {
                "data": {
                    "type": "match",
                    "id": "test-match-001",
                    "attributes": {
                        "seasonState": "progress",
                        "mapName": "Tiger_Main",
                        "gameMode": "squad"
                    },
                    "relationships": {
                        "assets": {
                            "data": [
                                {
                                    "type": "asset",
                                    "id": "asset-001"
                                }
                            ]
                        }
                    }
                },
                "included": [
                    {
                        "type": "asset",
                        "id": "asset-001",
                        "attributes": {
                            "URL": "https://telemetry-cdn.pubg.com/telemetry/asset-001.json"
                        }
                    }
                ]
            }
            """;
        JsonNode matchResponse = objectMapper.readTree(matchJson);
        when(pubgApiManager.requestMatch("test-match-001")).thenReturn(matchResponse);

        // when - 매치 처리
        matchService.processMatches(
                List.of("test-match-001"),
                currentSeason.getSeasonApiId()
        );

        // then
        assertThat(matchRepository.findByMatchApiId("test-match-001")).isPresent();
    }
}

