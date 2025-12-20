package com.junki.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long memberId;

	@Column(name = "account_id", length = 100)
	private String accountId;

	@Column(name = "clan_id", length = 100)
	private String clanId;

	@Column(name = "username", length = 50, nullable = false)
	private String username;

	@Column(name = "discord_name", length = 100)
	private String discordName;

	public void updateAccountId(String accountId) {
		this.accountId = accountId;
	}

	public void updateDiscordName(String discordName) {
		this.discordName = discordName;
	}

	public void updateClanId(String clanId) {
		this.clanId = clanId;
	}

	public boolean isLinked() {
		return accountId != null && !accountId.isEmpty();
	}
}

