package com.nlu.app.controller;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping("sign-cookies")
    public AppResponse<String> getFileSignedURL(HttpServletResponse response) {
        var cookies = fileService.signCookies();
        response.addHeader("Set-Cookie", cookies.policyHeaderValue() + "; Domain=.wjbu.online; HttpOnly");
        response.addHeader("Set-Cookie", cookies.keyPairIdHeaderValue() + "; Domain=.wjbu.online; HttpOnly");
        response.addHeader("Set-Cookie", cookies.signatureHeaderValue() + "; Domain=.wjbu.online; HttpOnly");
        return AppResponse.<String>builder()
                .result("OK")
                .build();
    }

    @PutMapping("file/{fileName}")
    public AppResponse<String> putFileSignedURL(@PathVariable String fileName) {
        return AppResponse.<String>builder()
                .result(fileService.uploadToTemp(fileName))
                .build();
    }

    @PostMapping("file/{key}")
    public AppResponse<String> saveFile(@PathVariable String key) {
        return AppResponse.<String>builder()
                .result(fileService.moveToInventory(key))
                .build();
    }
}
