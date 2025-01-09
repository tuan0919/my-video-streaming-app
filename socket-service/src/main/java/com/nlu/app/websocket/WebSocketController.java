package com.nlu.app.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.notification_service.request.SendMessageWsRequest;
import com.nlu.app.dto.MessageDTO;
import com.nlu.app.service.WebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebSocketController {
    WebSocketService webSocketService;
    private final SimpMessagingTemplate messagingTemplate;
    @PostMapping("/send")
    public void handleMessage(@RequestBody MessageDTO msg) {
        log.info("Send message to: {}", msg.getTopic());
        log.info("Message: {}", msg.getMessage());
        messagingTemplate.convertAndSend(msg.getTopic(), msg.getMessage());
    }

    @PostMapping("/ws")
    public AppResponse<String> sendMessageToClient(@RequestBody SendMessageWsRequest request) {
        var response = webSocketService.sendMessageToClient(request);
        return AppResponse.<String>builder()
                .result(response)
                .build();
    }
}