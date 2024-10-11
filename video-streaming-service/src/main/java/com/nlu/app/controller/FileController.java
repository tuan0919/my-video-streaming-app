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
    public AppResponse<SaveFileResponse> saveFile(@RequestBody SaveFileRequest request,
                                                        @RequestHeader("X-UserId") String userId,
                                                        @RequestHeader("X-Username") String username) {
        return AppResponse.<SaveFileResponse>builder().result(fileService.moveToInventory(request, userId, username)).build();
    }

    @PutMapping
    public AppResponse<SignedURLResponse> putFile(@RequestBody PutFileRequest request,
                                                        @RequestHeader("X-UserId") String userId,
                                                        @RequestHeader("X-Username") String username) {
        var response = fileService.uploadToTemp(request, userId, username);
        return AppResponse.<SignedURLResponse>builder()
                .result(response).build();
    }

    @GetMapping
    public AppResponse<SignedURLResponse> getFile(@RequestParam("key") String key) {
        var response = fileService.generateURL(key);
        return AppResponse.<SignedURLResponse>builder()
                .result(response).build();
    }
}
