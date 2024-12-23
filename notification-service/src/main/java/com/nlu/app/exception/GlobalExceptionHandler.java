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
    ResponseEntity<AppResponse<String>> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        var appResponse = AppResponse.<String>builder()
                .message(ErrorCode.UNKNOWN_EXCEPTION.getMessage())
                .code(ErrorCode.UNKNOWN_EXCEPTION.getCode())
                .build();
        return ResponseEntity.status(ErrorCode.UNKNOWN_EXCEPTION.getStatusCode())
                .body(appResponse);
    }

    @ExceptionHandler(ApplicationException.class)
    ResponseEntity<AppResponse<String>> handleApplicationException(ApplicationException ex) {
        ex.printStackTrace();
        var appResponse = AppResponse.<String>builder()
                .message(ex.getErrorCode().getMessage())
                .code(ex.getErrorCode().getCode())
                .build();
        return ResponseEntity.status(ex.getErrorCode().getStatusCode())
                .body(appResponse);
    }
}
