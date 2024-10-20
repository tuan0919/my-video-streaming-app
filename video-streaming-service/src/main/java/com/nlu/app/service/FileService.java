package com.nlu.app.service;

import com.nlu.app.common.share.dto.file_service.request.MoveFileRequest;
import com.nlu.app.common.share.dto.file_service.request.UploadFileRequest;
import com.nlu.app.common.share.dto.file_service.response.SignedURLResponse;
import com.nlu.app.configuration.WebClientBuilder;
import com.nlu.app.dto.request.PutFileRequest;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.response.SaveFileResponse;
import com.nlu.app.repository.FileWebClient;
import com.nlu.app.util.MyUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class FileService {
    @NonFinal
    FileWebClient fileWebClient;

    @Autowired
    private void setFileWebClient(@Qualifier("fileWebClient") WebClient webClient) {
        this.fileWebClient = WebClientBuilder.createClient(webClient, FileWebClient.class);
    }

    public String generateResourceURL (String key) {
        try {
            String link = fileWebClient.getFile(key).block().getResult().getLink();
            return link;
        } catch (WebClientResponseException e) {
            e.printStackTrace();
            throw MyUtils.convertException(e);
        }
    }

    public SaveFileResponse moveToInventory(SaveFileRequest request, String userId, String username) {
        try {
            String oldKey = "temp/"+username+"/"+request.getFilename();
            String extension = oldKey.substring(oldKey.lastIndexOf(".") + 1);
            String newKey = UUID.randomUUID().toString() + "." + extension;
            newKey = "inventory/"+username+"/"+newKey;
            var requestMoveFile = new MoveFileRequest(oldKey, newKey);
            fileWebClient.moveObject(requestMoveFile).block().getResult();
            return SaveFileResponse.builder()
                    .key(newKey)
                    .build();
        } catch (WebClientResponseException e) {
            throw MyUtils.convertException(e);
        }
    }

    public SignedURLResponse getUrlUploadToTemp (PutFileRequest request, String userId, String username) {
        String fileName = request.getFilename();
        String fileKey = "temp/"+username+"/"+fileName;
        UploadFileRequest uploadRequest = new UploadFileRequest(fileKey);
        try {
            return fileWebClient.getPutObjectURL(uploadRequest).block().getResult();
        } catch (WebClientException e) {
            throw MyUtils.convertException(e);
        }
    }
}
