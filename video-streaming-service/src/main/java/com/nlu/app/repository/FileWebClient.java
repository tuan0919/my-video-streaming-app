package com.nlu.app.repository;

import com.nlu.app.common.share.dto.file_service.request.MoveFileRequest;
import com.nlu.app.common.share.dto.file_service.request.UploadFileRequest;
import com.nlu.app.common.share.dto.file_service.response.SignedURLResponse;
import com.nlu.app.dto.AppResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import reactor.core.publisher.Mono;

public interface FileWebClient {
    @PutExchange("files/s3")
    Mono<AppResponse<SignedURLResponse>> getPutObjectURL (@RequestBody UploadFileRequest request);

    @PostExchange("files/s3")
    Mono<AppResponse<String>> moveObject(@RequestBody MoveFileRequest request);

    @GetExchange("files/{key}")
    Mono<AppResponse<SignedURLResponse>> getFile(@PathVariable("key") String key);
}