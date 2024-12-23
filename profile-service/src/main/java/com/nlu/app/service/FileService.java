package com.nlu.app.service;

import com.nlu.app.common.share.dto.file_service.request.MoveFileRequest;
import com.nlu.app.common.share.dto.file_service.request.UploadFileRequest;
import com.nlu.app.common.share.dto.file_service.response.SignedURLResponse;
import com.nlu.app.common.share.webclient.FileWebClient;
import com.nlu.app.configuration.WebClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
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
        return fileWebClient.getFile(key).block().getResult().getLink();
    }

    public Map<String, String> generateResourceURLs(List<String> keys) {
        var mapURLs = fileWebClient.getFiles(keys).block().getResult();
        Map<String, String> map = new HashMap<>();
        for (var key : keys) {
            map.put(key, mapURLs.get(key).getLink());
        }
        return map;
    }

    public String moveFile(MoveFileRequest request) {
        return fileWebClient.moveObject(request).block().getResult();
    }

    public SignedURLResponse getUrlUploadToTemp (UploadFileRequest request) {
        return fileWebClient.getPutObjectURL(request).block().getResult();
    }
}
