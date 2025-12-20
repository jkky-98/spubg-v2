package com.junki.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.junki.demo.entity.Member;
import com.junki.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {
	private final MemberRepository memberRepository;
	private final PubgApiManager pubgApiManager;

	/**
	 * 모든 멤버 조회
	 */
	@Transactional(readOnly = true)
	public List<Member> findAll() {
		return memberRepository.findAll();
	}

	/**
	 * 멤버 생성 (USERNAME만 입력)
	 */
	public Member createMember(String username) {
		Member member = Member.builder()
				.username(username)
				.build();
		return memberRepository.save(member);
	}

	/**
	 * DISCORD_NAME 업데이트
	 */
	public Member updateDiscordName(Long memberId, String discordName) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + memberId));
		member.updateDiscordName(discordName);
		return member;
	}

	/**
	 * PUBG API 연동 (ACCOUNT_ID 업데이트)
	 */
	public Member linkPubgAccount(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + memberId));

		try {
			JsonNode response = pubgApiManager.requestMember(member.getUsername());
			
			// PUBG API 응답에서 accountId 추출
			// 응답 구조: {"data": [{"id": "account.xxx", ...}]}
			JsonNode data = response.get("data");
			if (data != null && data.isArray() && data.size() > 0) {
				String accountId = data.get(0).get("id").asText();
				member.updateAccountId(accountId);
				
				// CLAN_ID도 함께 업데이트 (있는 경우)
				JsonNode attributes = data.get(0).get("attributes");
				if (attributes != null) {
					JsonNode clanIdNode = attributes.get("clanId");
					if (clanIdNode != null && !clanIdNode.isNull()) {
						member.updateClanId(clanIdNode.asText());
					}
				}
				
				log.info("[MemberService][linkPubgAccount] 멤버 연동 성공: {} -> {}", 
						member.getUsername(), accountId);
			} else {
				log.warn("[MemberService][linkPubgAccount] 플레이어를 찾을 수 없습니다: {}", 
						member.getUsername());
				throw new IllegalArgumentException("플레이어를 찾을 수 없습니다: " + member.getUsername());
			}
		} catch (Exception e) {
			log.error("[MemberService][linkPubgAccount] PUBG API 연동 실패: {}", e.getMessage());
			throw new RuntimeException("PUBG API 연동 실패: " + e.getMessage(), e);
		}

		return member;
	}
}

