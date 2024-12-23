package com.nlu.app.websocket;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.notification_service.request.SendMessageWsRequest;
import com.nlu.app.service.WebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebSocketController {
    WebSocketService webSocketService;
    private final SimpMessagingTemplate messagingTemplate;

    // Lắng nghe tin nhắn từ client gửi tới "/app/message"
    @MessageMapping("/message")
    public void handleMessage(@Payload String message, StompHeaderAccessor stompHeaderAccessor) {
        System.out.println("Received message: " + message);
        String username = stompHeaderAccessor.getSessionAttributes().get("userId").toString();
        String topic = String.format("/topic/%s/notification", username);
        messagingTemplate.convertAndSend(topic, "You sent: "+message);
    }

    @PostMapping("/ws")
    public AppResponse<String> sendMessageToClient(@RequestBody SendMessageWsRequest request) {
        var response = webSocketService.sendMessageToClient(request);
        return AppResponse.<String>builder()
                .result(response)
                .build();
    }
}