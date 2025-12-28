package com.junki.demo.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * RabbitMQ Match 처리 메시지 (단일 매치 처리용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchProcessMessage implements Serializable {
    private String matchApiId;

    private String seasonApiId;
}

