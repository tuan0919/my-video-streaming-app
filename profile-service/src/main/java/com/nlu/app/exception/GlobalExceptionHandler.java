package com.nlu.app.exception;

import com.nlu.app.dto.AppResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
@ResponseBody
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    AppResponse<String> handleRuntimeException(RuntimeException ex) {
        log.error("exception: ", ex);
        return AppResponse.<String>builder()
                .message(ErrorCode.UNKNOWN_EXCEPTION.getMessage())
                .code(ErrorCode.UNKNOWN_EXCEPTION.getCode())
                .build();
    }

    @ExceptionHandler(ApplicationException.class)
    ResponseEntity<AppResponse<?>> handleApplicationException(ApplicationException ex) {
        var response = AppResponse.<String>builder()
                .message(ex.getErrorCode().getMessage())
                .code(ex.getErrorCode().getCode())
                .build();
        return ResponseEntity.status(ex.getErrorCode().getStatusCode())
                .body(response);
    }

    @ExceptionHandler(ServiceException.class)
    ResponseEntity<AppResponse<?>> handleServiceException(ServiceException ex) {
        var body = AppResponse.builder()
                .message(ex.getMessage())
                .code(ex.getErrorCode())
                .build();
        return ResponseEntity.status(ex.getStatusCode())
                .body(body);
    }
}