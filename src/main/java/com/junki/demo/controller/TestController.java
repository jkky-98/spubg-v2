package com.junki.demo.controller;

import com.junki.demo.service.MatchService;
import com.junki.demo.service.SeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 테스트용 컨트롤러 (수동 테스트용)
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final SeasonService seasonService;
    private final MatchService matchService;

    /**
     * 시즌 동기화 수동 실행
     */
    @PostMapping("/seasons/sync")
    public ResponseEntity<Map<String, Object>> syncSeasons() {
        try {
            seasonService.syncSeasons();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "시즌 동기화 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 현재 시즌 조회
     */
    @GetMapping("/seasons/current")
    public ResponseEntity<Map<String, Object>> getCurrentSeason() {
        try {
            var season = seasonService.getCurrentSeason();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("season", Map.of(
                    "id", season.getId(),
                    "seasonApiId", season.getSeasonApiId(),
                    "current", season.isCurrent(),
                    "offSeason", season.isOffSeason()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

