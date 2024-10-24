package com.nlu.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class MyUtils {
    public static ServiceException convertException(Exception e) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // expected errors
            var webClientException = (WebClientResponseException)e;
            var applicationException = objectMapper.readValue(webClientException.getResponseBodyAsString(), AppResponse.class);
            var exception = ServiceException.builder()
                    .statusCode(webClientException.getStatusCode())
                    .message(applicationException.getMessage())
                    .errorCode(applicationException.getCode())
                    .build();
            return exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ServiceException("Something went wrong :(", HttpStatus.INTERNAL_SERVER_ERROR, 9999);
        }
    }
}
