package com.nlu.app.infracstructure.spring;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

@Controller
@RequiredArgsConstructor
public class WebSocketHandler {
    SimpMessagingTemplate messagingTemplate;
    public static HashMap<String, Queue<Object>> sessionStore = new HashMap<>();

    public void sendMessageToUser(String requestId, String topic, Object message) {
        if (sessionStore.containsKey(requestId)) {
            messagingTemplate.convertAndSendToUser(requestId, topic, message);
        } else {
            var queue = new ArrayDeque<>();
            queue.add(message);
            sessionStore.put(requestId, queue);
        }
    }
}