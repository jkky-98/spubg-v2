package com.junki.demo.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberMatchMessage implements Serializable {
    private Long matchId;           // match PK
    private String matchApiId;      // match API ID
}
