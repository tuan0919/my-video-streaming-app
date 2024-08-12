package com.nlu.app.controller;
import com.nlu.app.annotation.JwtToken;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.dto.request.PutFileRequest;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.response.SaveFileResponse;
import com.nlu.app.dto.response.SignedURLResponse;
import com.nlu.app.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileController {
    FileService fileService;

    @PostMapping
    public Mono<AppResponse<SaveFileResponse>> saveFile(@RequestBody SaveFileRequest request,
                                                        @JwtToken String token) {
        return fileService.moveToInventory(request, token)
                .map(response -> {
                    return AppResponse.<SaveFileResponse>builder()
                            .result(response).build();
                });
    }

    @PutMapping
    public Mono<AppResponse<SignedURLResponse>> putFile(@RequestBody PutFileRequest request,
                                                        @JwtToken String token) {
        return fileService.uploadToTemp(request, token)
                .map(response -> {
                    return AppResponse.<SignedURLResponse>builder()
                            .result(response).build();
                });
    }

    @GetMapping
    public Mono<AppResponse<SignedURLResponse>> getFile(@RequestParam("key") String key,
                                                        @JwtToken String token) {
        return fileService.generateURL(key)
                .map(response -> {
                    return AppResponse.<SignedURLResponse>builder()
                            .result(response).build();
                });
    }
}
