package com.junki.demo.entity;

import com.junki.demo.converter.GameMapConverter;
import com.junki.demo.enums.GameMap;
import com.junki.demo.enums.GameMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matches")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    @Column(name = "match_api_id", unique = true)
    private String matchApiId;

    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "asset_url")
    private String assetUrl;

    @Column(name = "map")
    @Convert(converter = GameMapConverter.class)
    private GameMap map;

    @Enumerated(EnumType.STRING)
    private GameMode gameMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;

    @Column(name = "analysis")
    private boolean analysis;

    public void finishAnalysis() {
        this.analysis = true;
    }
}
