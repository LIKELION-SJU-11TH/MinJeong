package com.study.board.model.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostJwtRes {
    private Long userId;
    private String accessToken;
    private String refreshToken;
}
