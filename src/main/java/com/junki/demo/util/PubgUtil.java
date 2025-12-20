package com.junki.demo.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PubgUtil {
	private static final String API_PATH = "https://api.pubg.com/shards/steam";

	@Value("${PUBG_API_KEY}")
	private String apiKey;

	public String getApiKey() {
		return "Bearer " + apiKey;
	}

	public String getBaseUrl() {
		return API_PATH;
	}

	public String getAccept() {
		return "application/vnd.api+json";
	}
}

