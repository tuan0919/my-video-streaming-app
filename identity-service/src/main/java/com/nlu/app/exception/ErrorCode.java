package com.nlu.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNKNOWN_EXCEPTION(9999, "Something went wrong :(.", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND(1001, "Resource not found, please check again!", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1002, "You are not authenticated!", HttpStatus.UNAUTHORIZED),
    USER_NOT_EXISTED(1003, "User is not existed!", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTED(1003, "User is already existed!", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
