package com.nlu.app.controller;

import com.amazonaws.services.s3.AmazonS3;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    AmazonS3 s3;
    @GetMapping("aws/testc")
    public void connect() {
        System.out.println(s3);
    }
}
