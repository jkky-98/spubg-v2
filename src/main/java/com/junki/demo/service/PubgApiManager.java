package com.junki.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junki.demo.util.PubgUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PubgApiManager {
	private final WebClient webClient;
	private final PubgUtil pubgUtil;
	private final ObjectMapper objectMapper;

	/**
	 * WebClient 요청을 처리하는 공통 메서드
	 * @param endpoint API 요청할 URI 경로 (예: "/seasons")
	 * @return JsonNode (응답 데이터)
	 */
	public JsonNode get(String endpoint) {

		String fullUrl = pubgUtil.getBaseUrl() + endpoint;
		log.info("[PubgApiManager][get] API 요청 시작: {}", fullUrl);

		try {
			String responseBody = webClient.get()
					.uri(fullUrl)
					.header(HttpHeaders.AUTHORIZATION, pubgUtil.getApiKey())
					.header(HttpHeaders.ACCEPT, pubgUtil.getAccept())
					.header(HttpHeaders.CONTENT_TYPE, "application/json")
					.retrieve()
					.bodyToMono(String.class)
					.doOnNext(r -> {
						log.info("[PubgApiManager][get] webClient 외부 API 요청 성공");
						log.info("[PubgApiManager][get] 응답 본문 (처음 500자): {}", 
								r != null && r.length() > 500 ? r.substring(0, 500) + "..." : r);
					})
					.doOnError(e -> {
						log.error("[PubgApiManager][get] webClient 외부 API 요청 실패: {}", e.getMessage());
						log.error("[PubgApiManager][get] 에러 상세: ", e);
					})
					.block();

			if (responseBody == null || responseBody.isEmpty()) {
				log.error("[PubgApiManager][get] 응답 본문이 비어있습니다");
				throw new RuntimeException("API 응답이 비어있습니다");
			}

			log.info("[PubgApiManager][get] 응답 본문 길이: {} bytes", responseBody.length());
			log.debug("[PubgApiManager][get] 전체 응답 본문: {}", responseBody);

			JsonNode jsonNode = objectMapper.readTree(responseBody);
			log.info("[PubgApiManager][get] JSON 파싱 성공");
			return jsonNode;
		} catch (Exception e) {
			log.error("[PubgApiManager][get] API 요청 또는 JSON 파싱 실패: {}", e.getMessage(), e);
			throw new RuntimeException("API 응답 파싱 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * 플레이어 정보 조회
	 * @param username 플레이어 이름
	 * @return JsonNode 플레이어 정보
	 */
	public JsonNode requestMember(String username) {
		return get("/players?filter[playerNames]=" + username);
	}

	/**
	 * 여러 플레이어 정보 조회
	 * @param usernames 플레이어 이름 리스트
	 * @return JsonNode 플레이어 정보
	 */
	public JsonNode requestManyMembers(java.util.List<String> usernames) {
		String joined = String.join(",", usernames);
		return get("/players?filter[playerNames]=" + joined);
	}

	/**
	 * 매치 정보 조회
	 * @param matchApiId 매치 API ID (예: "29c0c3c1-3b83-4e90-a29a-c0721efa014a")
	 * @return JsonNode 매치 정보
	 */
	public JsonNode requestMatch(String matchApiId) {
		return get("/matches/" + matchApiId);
	}

	/**
	 * 시즌 정보 조회
	 * @return JsonNode 시즌 정보
	 */
	public JsonNode requestSeasons() {
		return get("/seasons");
	}
}

