package com.study.board.service;

import com.study.board.common.entity.BaseEntity;
import com.study.board.common.exception.BaseException;
import com.study.board.common.response.BaseResponseStatus;
import com.study.board.entity.Board;
import com.study.board.entity.User;
import com.study.board.model.board.GetBoardRes;
import com.study.board.model.board.PostBoardReq;
import com.study.board.model.user.GetUserRes;
import com.study.board.repository.BoardRepository;
import com.study.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 저장
    public void saveBoard(Long userIdx, PostBoardReq postBoardReq) {
        User user = userRepository.findByIdAndState(userIdx, BaseEntity.State.ACTIVE)
                .orElseThrow(()->new BaseException(BaseResponseStatus.NON_EXIST_USER));
        log.info("USER 이름: {}", user.getName());

        if (postBoardReq.getContent().length() == 0 || postBoardReq.getContent().length() > 500) {
            throw new BaseException(BaseResponseStatus.CONTEXT_LENGTH_ERROR);
        }

        Board board = Board.builder()
                .title(postBoardReq.getTitle())
                .content(postBoardReq.getContent())
                .user(user)
                .build();
        log.info("게시글 빌드 성공: {}", board.getTitle());

        try {
            boardRepository.save(board);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_INSERT_ERROR);
        }
    }


    // 수정
    public void updateBoard(Long userId, Long boardId, PostBoardReq postBoardReq) {
        User user = userRepository.findByIdAndState(userId, BaseEntity.State.ACTIVE)
                .orElseThrow(()->new BaseException(BaseResponseStatus.NON_EXIST_USER));
        log.info("USER 이름: {}", user.getName());

        Board board = boardRepository.findByIdAndState(boardId, BaseEntity.State.ACTIVE)
                .orElseThrow(()-> new BaseException(BaseResponseStatus.NON_EXIST_ARTICLE));

        if (user != board.getUser()) {
            throw new BaseException(BaseResponseStatus.NO_AUTH);
        }
        if (postBoardReq.getContent().length() == 0 || postBoardReq.getContent().length() > 500) {
            throw new BaseException(BaseResponseStatus.CONTEXT_LENGTH_ERROR);
        }

        board.updateBoard(postBoardReq.getTitle(), postBoardReq.getContent());
        boardRepository.flush();        // 변경된 상태를 데이터베이스에 적용
    }


    // 삭제
    public void deleteBoard(Long userIdx, Long boardIdx) {
        User user = userRepository.findByIdAndState(userIdx, BaseEntity.State.ACTIVE)
                .orElseThrow(()-> new BaseException(BaseResponseStatus.NON_EXIST_USER));

        Board board = boardRepository.findByIdAndState(boardIdx, BaseEntity.State.ACTIVE)
                .orElseThrow(()-> new BaseException(BaseResponseStatus.NON_EXIST_ARTICLE));

        if (user != board.getUser()) {
            throw new BaseException(BaseResponseStatus.NO_AUTH);
        }

        boardRepository.delete(board);
        boardRepository.flush();
    }


    // 게시물 전체 조회
    public List<GetBoardRes> viewBoards(Pageable pageable) {
        Page<Board> boards = boardRepository.findAll(pageable);

        List<GetBoardRes> getBoardResList = boards.getContent().stream().map(board -> {
            GetBoardRes getBoardRes = new GetBoardRes();
            getBoardRes.setTitle(board.getTitle());
            getBoardRes.setContent(board.getContent());
            getBoardRes.setWriter(new GetUserRes(board.getUser()));
            getBoardRes.setCreatedAt(board.getCreatedAt());

            return getBoardRes;
        }).collect(Collectors.toList());

        return getBoardResList;
    }


    // 게시물 단일 조회
    public GetBoardRes viewSingleBoard(Long boardId) {
        Board board = boardRepository.findByIdAndState(boardId, BaseEntity.State.ACTIVE)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NON_EXIST_ARTICLE));

        GetBoardRes getBoardRes = GetBoardRes.builder()
                .writer(new GetUserRes(board.getUser()))
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .build();

        return getBoardRes;
    }
}
