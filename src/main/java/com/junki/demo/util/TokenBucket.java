package com.junki.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TokenBucket {
	private static final int MAX_TOKENS = 10;
	private final Semaphore tokens = new Semaphore(MAX_TOKENS);

	/** 즉시 소비 시도 */
	public boolean tryConsume() {
		return tokens.tryAcquire();
	}

	/** 지정 시간만큼 토큰이 나올 때까지 대기 후 소비 시도 */
	public boolean tryConsume(long timeout, TimeUnit unit) throws InterruptedException {
		return tokens.tryAcquire(1, timeout, unit);
	}

	/** 토큰 한 개 보충 */
	public void refill() {
		if (tokens.availablePermits() < MAX_TOKENS) {
			tokens.release();
			log.debug("[TokenBucket][refill] 🆕 토큰 추가 (현재 토큰 수: {}/{})",
					tokens.availablePermits(), MAX_TOKENS);
		}
	}

	/** 모든 토큰을 최대치로 보충 (1분마다 호출) */
	public void refillAll() {
		int current = tokens.availablePermits();
		int needed = MAX_TOKENS - current;
		if (needed > 0) {
			tokens.release(needed);
			log.debug("[TokenBucket][refillAll] 🆕 모든 토큰 보충 완료 (현재 토큰 수: {}/{})",
					tokens.availablePermits(), MAX_TOKENS);
		}
	}

	public int getAvailableTokens() {
		return tokens.availablePermits();
	}

	public int getMaxTokens() {
		return MAX_TOKENS;
	}
}

