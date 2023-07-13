package com.study.board.controller;

import com.study.board.common.exception.BaseException;
import com.study.board.common.response.BaseResponse;
import com.study.board.model.board.GetBoardRes;
import com.study.board.model.board.PostBoardReq;
import com.study.board.service.BoardService;
import com.study.board.util.JwtUtils;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class BoardController {
    private final BoardService boardService;
    private final JwtUtils jwtUtils;

    // 게시글 생성
    @PostMapping("/board/add")
    public BaseResponse<String> createBoard(@RequestBody PostBoardReq postBoardReq) {
        Long userIdx = jwtUtils.getUserIdV2();

        try {
            boardService.saveBoard(userIdx, postBoardReq);
            return new BaseResponse<>("게시물을 등록하였습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    // 게시글 수정
    public BaseResponse<String> updateBoard(@RequestBody PostBoardReq postBoardReq, @RequestParam("boardId") Long boardIdx) {
        Long userIdx = jwtUtils.getUserIdV2();

        try {
            boardService.updateBoard(userIdx, boardIdx, postBoardReq);
            return new BaseResponse<>("게시물을 수정하였습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    // 게시글 삭제
    @DeleteMapping("/board")
    public BaseResponse<String> deleteBoard(@RequestParam("boardId") Long boardIdx) {
        Long userIdx = jwtUtils.getUserIdV2();

        try {
            boardService.deleteBoard(userIdx, boardIdx);
            return new BaseResponse<>("게시물을 삭제하였습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    // 게시글 전체 조회
    @GetMapping("/")
    public BaseResponse<List<GetBoardRes>> viewBoards(Pageable pageable) {
        jwtUtils.getUserIdV2();
        try {
            List<GetBoardRes> boardResList = boardService.viewBoards(pageable);
            return new BaseResponse<>(boardResList);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }


    // 게시글 단일 조회
    public BaseResponse<GetBoardRes> viewSingleBoard(@RequestParam("boardId") Long boardIdx) {
        jwtUtils.getUserIdV2();

        try {
            GetBoardRes boardRes = boardService.viewSingleBoard(boardIdx);
            return new BaseResponse<>(boardRes);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }
}
