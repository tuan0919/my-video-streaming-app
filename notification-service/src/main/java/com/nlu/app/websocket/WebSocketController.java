package com.nlu.app.websocket;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.notification_service.request.SendMessageWsRequest;
import com.nlu.app.service.WebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebSocketController {
    WebSocketService webSocketService;

    @PostMapping("/ws")
    public AppResponse<String> sendMessageToClient(@RequestBody SendMessageWsRequest request) {
        var response = webSocketService.sendMessageToClient(request);
        return AppResponse.<String>builder()
                .result(response)
                .build();
    }
}