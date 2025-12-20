package com.junki.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "season_id")
    private Long id;

    @Column(name = "season_api_id")
    private String seasonApiId;

    @Column(name = "current")
    private boolean current;

    @Column(name = "off_season")
    private boolean offSeason;
}
