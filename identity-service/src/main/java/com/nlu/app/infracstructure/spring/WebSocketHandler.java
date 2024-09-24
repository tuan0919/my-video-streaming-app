package com.nlu.app.infracstructure.spring;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketHandler {

    @MessageMapping("/hello")
    // Nhận tin nhắn từ client tại /app/hello
    @SendTo("/topic/greetings") // Gửi tin nhắn đến tất cả client đã đăng ký tại /topic/greetings
    public String greeting(String message) throws Exception {
        return "Hello, " + message + "!";
    }
}