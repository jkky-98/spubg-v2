package com.junki.demo.repository;

import com.junki.demo.entity.Match;
import com.junki.demo.entity.Member;
import com.junki.demo.entity.MemberMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberMatchRepository extends JpaRepository<MemberMatch, Long> {

    boolean existsByMemberAndMatch(Member member, Match match);
}
