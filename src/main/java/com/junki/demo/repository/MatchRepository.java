package com.junki.demo.repository;

import com.junki.demo.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByMatchApiId(String matchApiId);
    List<Match> findByMatchApiIdIn(Set<String> matchApiIds);
    boolean existsByMatchApiId(String matchApiId);
    List<Match> findByAnalysisFalse();
}

