package com.study.board.model.board;

import com.study.board.model.user.GetUserRes;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GetBoardRes {
    private GetUserRes writer;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    @Builder
    public GetBoardRes(GetUserRes writer, String title, String content, LocalDateTime createdAt) {
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
}
