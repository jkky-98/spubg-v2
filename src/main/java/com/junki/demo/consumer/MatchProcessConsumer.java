package com.junki.demo.consumer;

import com.junki.demo.message.MatchProcessMessage;
import com.junki.demo.service.MatchService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
/**
 * RabbitMQ Match 처리 Consumer
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchProcessConsumer {
    private final MatchService matchService;
    private final Bucket pubgApiBucket;

    @RabbitListener(queues = "match.process.queue", concurrency = "1")
    public void processMatch(MatchProcessMessage message) {
        String matchApiId = message.getMatchApiId();
        String seasonApiId = message.getSeasonApiId();

        try {
            log.info("[MatchProcessConsumer] 매치 처리 대기 중... ID: {}", matchApiId);

            // 토큰 소모 대기 (InterruptedException 발생 가능)
            pubgApiBucket.asBlocking().consume(1);

            // 실제 서비스 로직 실행
            matchService.processMatch(matchApiId, seasonApiId);
            log.info("[MatchProcessConsumer] 매치 처리 완료: {}", matchApiId);

        } catch (InterruptedException e) {
            log.warn("[MatchProcessConsumer] 대기 중 인터럽트 발생 - 처리를 중단합니다. ID: {}", matchApiId);
            // 중요: 현재 스레드의 인터럽트 상태를 다시 설정 (상위 호출자나 프레임워크가 인지할 수 있도록)
            Thread.currentThread().interrupt();

            // 메시지를 다시 큐로 돌려보내거나(Requeue) 버릴지 결정해야 함
            // 여기서는 예외를 던져 RabbitMQ가 처리하게 합니다.
            throw new RuntimeException("Consumer interrupted", e);

        } catch (Exception e) {
            log.error("[MatchProcessConsumer] 매치 처리 실패: {}", matchApiId, e);
            throw e; // RabbitMQ Retry 설정 가동
        }
    }
}