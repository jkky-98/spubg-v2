package com.junki.demo.service;

import com.junki.demo.exception.TokenUnavailableException;
import com.junki.demo.util.TokenBucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenManager {
	private static final long MAX_WAIT_TIME_MS = 1_000_000;
	private final TokenBucket bucket;

	/**
	 * 토큰 소비 시도
	 * @throws TokenUnavailableException 토큰을 확보하지 못한 경우
	 */
	public void consume() {
		try {
			boolean acquired = bucket.tryConsume(MAX_WAIT_TIME_MS, TimeUnit.MILLISECONDS);
			if (!acquired) {
				log.debug("[TokenManager][consume] 토큰 대기시간 초과 ({} ms)", MAX_WAIT_TIME_MS);
				throw new TokenUnavailableException("토큰을 " + MAX_WAIT_TIME_MS + "ms 안에 확보하지 못했습니다");
			} else {
				log.debug("[TokenManager][consume] 토큰 소비되었습니다. 남은 토큰: {}",
						bucket.getAvailableTokens());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("[TokenManager][consume] 인터럽트 발생", e);
		}
	}

	/**
	 * 1분마다 모든 토큰 보충 (PUBG API는 1분에 10개 요청 제한이므로)
	 * 1분이 지나면 다시 10개 모두 사용 가능
	 */
	@Scheduled(fixedRate = 60_000) // 1분 = 60초 = 60,000ms
	public void refillTokens() {
		bucket.refillAll();
	}

	/**
	 * 현재 사용 가능한 토큰 수 조회
	 */
	public int getAvailableTokens() {
		return bucket.getAvailableTokens();
	}

	/**
	 * 최대 토큰 수 조회
	 */
	public int getMaxTokens() {
		return bucket.getMaxTokens();
	}
}

