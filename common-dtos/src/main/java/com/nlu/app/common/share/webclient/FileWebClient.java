package com.nlu.app.common.share.webclient;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.file_service.request.MoveFileRequest;
import com.nlu.app.common.share.dto.file_service.request.UploadFileRequest;
import com.nlu.app.common.share.dto.file_service.response.SignedURLResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import reactor.core.publisher.Mono;

public interface FileWebClient {
    @PutExchange("files/s3")
    Mono<AppResponse<SignedURLResponse>> getPutObjectURL (@RequestBody UploadFileRequest request);

    @PostExchange("files/s3")
    Mono<AppResponse<String>> moveObject(@RequestBody MoveFileRequest request);

    @GetExchange("files/s3")
    Mono<AppResponse<SignedURLResponse>> getFile(@RequestParam("key") String key);
}