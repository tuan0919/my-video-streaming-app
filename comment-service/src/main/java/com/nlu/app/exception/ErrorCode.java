package com.nlu.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNKNOWN_EXCEPTION(9999, "Something went wrong :(.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1002, "You are not authenticated!", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(1003, "You are not authorized to access!", HttpStatus.UNAUTHORIZED),
    PARENT_COMMENT_NOT_EXISTED(1004, "The comment you're replied is not exists!", HttpStatus.NOT_FOUND),
    TARGET_VIDEO_NOT_EXISTED(1005, "The video you are comment on is not existed!", HttpStatus.BAD_REQUEST)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
