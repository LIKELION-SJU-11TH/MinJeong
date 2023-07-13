package com.study.board.model.board;

import lombok.Getter;

@Getter
public class PostBoardReq {
    private String title;
    private String content;

    public PostBoardReq() {
    }

    public PostBoardReq(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
