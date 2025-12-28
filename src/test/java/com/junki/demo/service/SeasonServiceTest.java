package com.junki.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junki.demo.entity.Season;
import com.junki.demo.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeasonService 테스트")
class SeasonServiceTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private PubgApiManager pubgApiManager;

    @InjectMocks
    private SeasonService seasonService;

    private ObjectMapper objectMapper;
    private JsonNode seasonResponse;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        
        String json = """
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
        
        seasonResponse = objectMapper.readTree(json);
    }

    @Test
    @DisplayName("시즌 동기화 - 새 시즌 추가")
    void syncSeasons_NewSeason() {
        // given
        when(pubgApiManager.requestSeasons()).thenReturn(seasonResponse);
        when(seasonRepository.findByCurrentTrue()).thenReturn(Optional.empty());
        when(seasonRepository.findBySeasonApiId("division.bro.official.pc-2024-01"))
                .thenReturn(Optional.empty());
        when(seasonRepository.save(any(Season.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        seasonService.syncSeasons();

        // then
        verify(seasonRepository, times(1)).save(any(Season.class));
    }

    @Test
    @DisplayName("시즌 동기화 - 기존 시즌 업데이트")
    void syncSeasons_UpdateExisting() {
        // given
        Season existingSeason = Season.builder()
                .id(1L)
                .seasonApiId("division.bro.official.pc-2024-01")
                .current(false)
                .offSeason(false)
                .build();

        when(pubgApiManager.requestSeasons()).thenReturn(seasonResponse);
        when(seasonRepository.findByCurrentTrue()).thenReturn(Optional.empty());
        when(seasonRepository.findBySeasonApiId("division.bro.official.pc-2024-01"))
                .thenReturn(Optional.of(existingSeason));
        when(seasonRepository.save(any(Season.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        seasonService.syncSeasons();

        // then
        verify(seasonRepository, times(1)).save(existingSeason);
        assertThat(existingSeason.isCurrent()).isTrue();
    }
}

