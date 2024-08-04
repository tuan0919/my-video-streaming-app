package com.nlu.app.controller;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class FileController {
    FileService fileService;
    @GetMapping("file")
    public AppResponse<String> getFileSignedURL(@RequestParam String key) {
        return AppResponse.<String>builder()
                .result(fileService.generateURL(key))
                .build();
    }

    @PutMapping("file/{fileName}")
    public AppResponse<String> putFileSignedURL(@PathVariable String fileName) {
        return AppResponse.<String>builder()
                .result(fileService.signUpload(fileName))
                .build();
    }
}
