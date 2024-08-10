package com.nlu.app.controller;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.PutFileRequest;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.response.SaveFileResponse;
import com.nlu.app.dto.response.SignedURLResponse;
import com.nlu.app.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    FileService fileService;

    @PostMapping
    public Mono<AppResponse<SaveFileResponse>> saveFile(@RequestBody SaveFileRequest request) {
        return fileService.moveToInventory(request)
                .map(response -> {
                    return AppResponse.<SaveFileResponse>builder()
                            .result(response).build();
                });
    }

    @PutMapping
    public Mono<AppResponse<SignedURLResponse>> saveFile(@RequestBody PutFileRequest request) {
        return fileService.uploadToTemp(request)
                .map(response -> {
                    return AppResponse.<SignedURLResponse>builder()
                            .result(response).build();
                });
    }

    @GetMapping
    public Mono<AppResponse<SignedURLResponse>> getFile(@RequestParam("key") String key) {
        return fileService.generateURL(key)
                .map(response -> {
                    return AppResponse.<SignedURLResponse>builder()
                            .result(response).build();
                });
    }
}
