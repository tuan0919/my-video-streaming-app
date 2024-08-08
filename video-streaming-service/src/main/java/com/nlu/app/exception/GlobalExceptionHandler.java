package com.nlu.app.exception;

import com.nlu.app.dto.AppResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    AppResponse<?> handleRuntimeException(RuntimeException ex) {
        log.error("exception: ", ex);
        return AppResponse.builder()
                .message(ErrorCode.UNKNOWN_EXCEPTION.getMessage())
                .code(ErrorCode.UNKNOWN_EXCEPTION.getCode())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    AppResponse<?> handleException(Exception ex) {
        log.error("exception: ", ex);
        return AppResponse.builder()
                .message(ErrorCode.UNKNOWN_EXCEPTION.getMessage())
                .code(ErrorCode.UNKNOWN_EXCEPTION.getCode())
                .build();
    }

    @ExceptionHandler(ApplicationException.class)
    @ResponseBody
    AppResponse<?> handleApplicationException(ApplicationException ex) {
        log.error("exception: ", ex);
        return AppResponse.builder()
                .message(ex.getErrorCode().getMessage())
                .code(ex.getErrorCode().getCode())
                .build();
    }
}
