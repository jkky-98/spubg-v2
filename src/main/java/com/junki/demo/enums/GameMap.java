package com.junki.demo.enums;

import java.util.HashMap;
import java.util.Map;

public enum GameMap {
    BALTIC_MAIN("Baltic_Main", "Erangel (Remastered)"),
    CHIMERA_MAIN("Chimera_Main", "Paramo"),
    DESERT_MAIN("Desert_Main", "Miramar"),
    DIHOR_OTOK_MAIN("DihorOtok_Main", "Vikendi"),
    ERANGEL_MAIN("Erangel_Main", "Erangel"),
    HEAVEN_MAIN("Heaven_Main", "Haven"),
    KIKI_MAIN("Kiki_Main", "Deston"),
    RANGE_MAIN("Range_Main", "Camp Jackal"),
    SAVAGE_MAIN("Savage_Main", "Sanhok"),
    SUMMERLAND_MAIN("Summerland_Main", "Karakin"),
    TIGER_MAIN("Tiger_Main", "Taego"),
    NEON_MAIN("Neon_Main", "Rondo"),
    UNKNOWN("Unknown", "Unknown"); // 기본값

    private static final Map<String, GameMap> LOOKUP = new HashMap<>();

    static {
        for (GameMap map : GameMap.values()) {
            LOOKUP.put(map.mapKey, map);
        }
    }

    private final String mapKey;
    private final String displayName;

    GameMap(String mapKey, String displayName) {
        this.mapKey = mapKey;
        this.displayName = displayName;
    }

    public static String getDisplayName(String mapKey) {
        return LOOKUP.getOrDefault(mapKey, UNKNOWN).displayName;
    }

    /**
     * mapKey 문자열로부터 GameMap enum을 찾아 반환
     * @param mapKey 맵 키 (예: "Erangel_Main")
     * @return GameMap enum, 없으면 UNKNOWN
     */
    public static GameMap fromString(String mapKey) {
        if (mapKey == null || mapKey.isEmpty()) {
            return UNKNOWN;
        }
        return LOOKUP.getOrDefault(mapKey, UNKNOWN);
    }

    /**
     * GameMap enum의 mapKey 반환
     * @return mapKey 문자열
     */
    public String getMapKey() {
        return mapKey;
    }

    /**
     * GameMap enum의 displayName 반환
     * @return displayName 문자열
     */
    public String getDisplayName() {
        return displayName;
    }
}

