package com.nlu.app.exception;

import com.nlu.app.service.FileService;
import com.nlu.app.util.MyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice(basePackageClasses = {FileService.class})
@Slf4j
@ResponseBody
public class ServiceExceptionConverter {
    @ExceptionHandler(WebClientResponseException.class)
    void handle(WebClientResponseException ex) {
        log.error("exception: ", ex);
        throw MyUtils.convertException(ex);
    }
}