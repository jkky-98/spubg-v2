package com.junki.demo.consumer;

import com.junki.demo.message.MemberMatchMessage;
import com.junki.demo.service.MemberMatchService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberMatchConsumer {
    private final MemberMatchService memberMatchService;
    private final Bucket pubgApiBucket;

    @RabbitListener(queues = "match.analysis.queue", concurrency = "1")
    public void handleMatchAnalysis(MemberMatchMessage message) throws InterruptedException {
        log.info("[MatchAnalysisConsumer] 분석 시작: {}", message.getMatchApiId());

        // API 속도 제한 준수 (6초당 1개)
        pubgApiBucket.asBlocking().consume(1);

        try {
            memberMatchService.analyzeMatch(message.getMatchId(), message.getMatchApiId());
        } catch (Exception e) {
            log.error("[MatchAnalysisConsumer] 분석 실패: {}", message.getMatchApiId(), e);
            throw e;
        }
    }
}
