package com.junki.demo.controller;

import com.junki.demo.entity.Member;
import com.junki.demo.service.MemberService;
import com.junki.demo.service.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final MemberService memberService;
	private final TokenManager tokenManager;

	/**
	 * 관리 페이지 메인
	 */
	@GetMapping
	public String adminPage(Model model) {
		try {
			model.addAttribute("members", memberService.findAll());
			model.addAttribute("availableTokens", tokenManager.getAvailableTokens());
			model.addAttribute("maxTokens", tokenManager.getMaxTokens());
			return "admin/members";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "error";
		}
	}

	/**
	 * 테스트용 엔드포인트 (컨트롤러 등록 확인)
	 */
	@GetMapping("/test")
	@ResponseBody
	public Map<String, Object> test() {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "AdminController is working!");
		response.put("timestamp", System.currentTimeMillis());
		return response;
	}

	/**
	 * 멤버 추가
	 */
	@PostMapping("/members")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> createMember(@RequestParam String username) {
		try {
			Member member = memberService.createMember(username);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("member", member);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * DISCORD_NAME 업데이트
	 */
	@PutMapping("/members/{id}/discord-name")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> updateDiscordName(
			@PathVariable Long id,
			@RequestParam String discordName) {
		try {
			Member member = memberService.updateDiscordName(id, discordName);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("member", member);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * PUBG API 연동
	 */
	@PostMapping("/members/{id}/link")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> linkPubgAccount(@PathVariable Long id) {
		try {
			Member member = memberService.linkPubgAccount(id);
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("member", member);
			response.put("availableTokens", tokenManager.getAvailableTokens());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", e.getMessage());
			response.put("availableTokens", tokenManager.getAvailableTokens());
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * 토큰 상태 조회 (실시간 업데이트용)
	 */
	@GetMapping("/tokens/status")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getTokenStatus() {
		Map<String, Object> response = new HashMap<>();
		response.put("availableTokens", tokenManager.getAvailableTokens());
		response.put("maxTokens", tokenManager.getMaxTokens());
		return ResponseEntity.ok(response);
	}
}

