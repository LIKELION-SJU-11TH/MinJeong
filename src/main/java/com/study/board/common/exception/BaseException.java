package com.study.board.common.exception;

import com.study.board.common.response.BaseResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseException extends RuntimeException {
    private BaseResponseStatus status;

    public BaseException(BaseResponseStatus status){
        super(status.getMassage());
        this.status = status;
    }
}
