package com.junki.demo.converter;

import com.junki.demo.enums.GameMap;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GameMapConverter implements AttributeConverter<GameMap, String> {

    @Override
    public String convertToDatabaseColumn(GameMap gameMap) {
        if (gameMap == null) {
            return null;
        }
        return gameMap.getMapKey();
    }

    @Override
    public GameMap convertToEntityAttribute(String mapKey) {
        return GameMap.fromString(mapKey);
    }
}

