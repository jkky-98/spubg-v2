package com.junki.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.junki.demo.entity.Season;
import com.junki.demo.repository.SeasonRepository;
import com.junki.demo.util.SeasonResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeasonService {
    private final SeasonRepository seasonRepository;
    private final PubgApiManager pubgApiManager;

    /**
     * 시즌 동기화 (PUBG API에서 시즌 정보를 가져와서 DB 업데이트)
     */
    public void syncSeasons() {
        log.info("[SeasonService][syncSeasons] 시즌 동기화 시작");

        try {
            JsonNode response = pubgApiManager.requestSeasons();
            List<SeasonResponseParser.SeasonData> seasonDataList = SeasonResponseParser.extractSeasons(response);

            // 기존 current 시즌을 false로 변경
            seasonRepository.findByCurrentTrue()
                    .ifPresent(season -> {
                        season.updateCurrent(false);
                        seasonRepository.save(season);
                    });

            // 새 시즌 정보 저장 또는 업데이트
            for (SeasonResponseParser.SeasonData data : seasonDataList) {
                seasonRepository.findBySeasonApiId(data.seasonApiId())
                        .ifPresentOrElse(
                                existing -> {
                                    // 기존 시즌 업데이트
                                    existing.updateCurrent(data.isCurrent());
                                    existing.updateOffSeason(data.isOffseason());
                                    seasonRepository.save(existing);
                                    log.info("[SeasonService][syncSeasons] 시즌 업데이트: {}", data.seasonApiId());
                                },
                                () -> {
                                    // 새 시즌 추가
                                    Season newSeason = Season.builder()
                                            .seasonApiId(data.seasonApiId())
                                            .current(data.isCurrent())
                                            .offSeason(data.isOffseason())
                                            .build();
                                    seasonRepository.save(newSeason);
                                    log.info("[SeasonService][syncSeasons] 시즌 추가: {}", data.seasonApiId());
                                }
                        );
            }

            log.info("[SeasonService][syncSeasons] 시즌 동기화 완료. 총 {}개 시즌 처리", seasonDataList.size());
        } catch (Exception e) {
            log.error("[SeasonService][syncSeasons] 시즌 동기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("시즌 동기화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 현재 활성화된 시즌 조회
     */
    @Transactional(readOnly = true)
    public Season getCurrentSeason() {
        return seasonRepository.findByCurrentTrue()
                .orElseThrow(() -> new IllegalStateException("현재 활성화된 시즌이 없습니다"));
    }
}

