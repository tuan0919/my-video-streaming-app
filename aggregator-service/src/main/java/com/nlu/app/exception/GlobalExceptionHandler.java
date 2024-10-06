package com.nlu.app.exception;

import com.nlu.app.dto.AppResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
@Order(-2)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    Mono<AppResponse<?>> handleRuntimeException(RuntimeException ex, ServerWebExchange exchange) {
        log.error("runtime exception: ", ex);
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return Mono.just(AppResponse.builder()
                .message(ErrorCode.UNKNOWN_EXCEPTION.getMessage())
                .code(ErrorCode.UNKNOWN_EXCEPTION.getCode())
                .build());
    }

    @ExceptionHandler({ApplicationException.class})
    @ResponseBody
    public Mono<AppResponse<?>> handleApplicationException(ApplicationException ex, ServerWebExchange exchange) {
        log.info("application exception: {}", ex.getMessage());
        exchange.getResponse().setStatusCode(ex.getErrorCode().getStatusCode());
        return Mono.just(AppResponse.builder()
                .message(ex.getErrorCode().getMessage())
                .code(ex.getErrorCode().getCode())
                .build());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    Mono<AppResponse<?>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("exception: ", ex);
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return Mono.just(AppResponse.builder()
                .message(ErrorCode.UNKNOWN_EXCEPTION.getMessage())
                .code(ErrorCode.UNKNOWN_EXCEPTION.getCode())
                .build());
    }

}
