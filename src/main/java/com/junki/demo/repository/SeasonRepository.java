package com.junki.demo.repository;

import com.junki.demo.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findBySeasonApiId(String seasonApiId);
    Optional<Season> findByCurrentTrue();
}

