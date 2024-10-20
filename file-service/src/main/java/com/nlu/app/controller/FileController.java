package com.nlu.app.controller;
import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.file_service.request.MoveFileRequest;
import com.nlu.app.common.share.dto.file_service.request.UploadFileRequest;
import com.nlu.app.common.share.dto.file_service.response.SignedURLResponse;
import com.nlu.app.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileController {
    FileService fileService;

    @PutMapping
    public AppResponse<SignedURLResponse> getPutObjectURL(@RequestBody UploadFileRequest request) {
        var response = fileService.signedUploadURL(request);
        return AppResponse.<SignedURLResponse>builder()
                .result(response).build();
    }

    @PostMapping
    public AppResponse<String> moveObject(@RequestBody MoveFileRequest request) {
        var response = fileService.moveFile(request);
        return AppResponse.<String>builder()
                .result(response).build();
    }

    @GetMapping("/{key}")
    public AppResponse<SignedURLResponse> getFile(@PathVariable("key") String key) {
        var response = fileService.generateURL(key);
        return AppResponse.<SignedURLResponse>builder()
                .result(response).build();
    }
}