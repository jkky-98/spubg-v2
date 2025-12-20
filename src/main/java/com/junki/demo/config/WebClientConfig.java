package com.junki.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public WebClient webClient() {
		// String codec만 사용하도록 설정 (JsonNode 역직렬화 문제 방지)
		ExchangeStrategies strategies = ExchangeStrategies.builder()
				.codecs(configurer -> {
					ClientCodecConfigurer.ClientDefaultCodecs codecs = configurer.defaultCodecs();
					codecs.maxInMemorySize(10 * 1024 * 1024); // 10MB
				})
				.build();

		return WebClient.builder()
				.exchangeStrategies(strategies)
				.build();
	}
}

