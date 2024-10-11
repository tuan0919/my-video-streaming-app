package com.nlu.app.exception;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatusCode;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ServiceException extends RuntimeException {
    String message;
    HttpStatusCode statusCode;
    Integer errorCode;

    public ServiceException(String message, HttpStatusCode statusCode, Integer errorCode) {
        this.message = message;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
}
