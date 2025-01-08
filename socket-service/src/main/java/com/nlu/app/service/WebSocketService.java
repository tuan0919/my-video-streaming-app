package com.nlu.app.service;

import com.nlu.app.common.share.dto.notification_service.request.SendMessageWsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messageBus;
    public String sendMessageToClient(SendMessageWsRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("payload", request.getPayload());
        message.put("action", request.getAction());
        String topic = request.getTopic();
        messageBus.convertAndSend(topic, message);
        return "OK";
    }
}
