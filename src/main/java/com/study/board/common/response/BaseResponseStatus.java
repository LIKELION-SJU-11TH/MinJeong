package com.study.board.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseResponseStatus {         // HttpStatus 코드
    // 성공 코드 2xx
    SUCCESS(true, HttpStatus.OK.value(), "요청에 성공하였습니다."),

    // 4xx 에러
    NO_AUTH(false, HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."),
    EXIST_EMAIL(false, HttpStatus.CONFLICT.value(), "이미 존재하는 회원입니다."),
    NON_EXIST_USER(false, HttpStatus.NOT_FOUND.value(), "존재하지 않는 회원입니다."),
    NON_EXIST_ARTICLE(false, HttpStatus.NOT_FOUND.value(), "존재하지 않는 게시글입니다."),
    NO_JWT(false, HttpStatus.BAD_REQUEST.value(), "JWT 토큰이 존재하지 않습니다."),
    INVALID_TOKEN(false, HttpStatus.BAD_REQUEST.value(), "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(false, HttpStatus.BAD_REQUEST.value(), "만료된 토큰입니다."),
    CONTEXT_LENGTH_ERROR(false, HttpStatus.BAD_REQUEST.value(), "내용은 0자 이상 500자 이하까지 입력할 수 있습니다"),
    NOT_MATCH_PASSWORD(false, HttpStatus.UNAUTHORIZED.value(), "비밀번호가 일치하지 않습니다."),
    NO_SESSION_ID(false, HttpStatus.BAD_REQUEST.value(), "세션아이디가 존재하지 않습니다."),

    // 5xx 에러
    DATABASE_INSERT_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "데이터베이스 저장에 실패하였습니다"),
    PASSWORD_ENCRYPTION_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "비밀번호 암호화에 실패하였습니다."),
    DATABASE_DELETE_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "데이터베이스 삭제에 실패하였습니다."),
    REDIRECT_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "리다이렉트에 실패하였습니다.");

    /*
    isSuccess: 요청의 성공/실패
    code: Http Status Code
    message: 설명
     */


    private final boolean isSuccess;
    private final int code;
    private final String massage;

    BaseResponseStatus(boolean isSuccess, int code, String massage) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.massage = massage;
    }
}
