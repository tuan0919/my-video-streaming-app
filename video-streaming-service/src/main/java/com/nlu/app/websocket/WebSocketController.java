package com.nlu.app.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {

    // Nhận tin nhắn từ client tại "/app/message" và trả về kết quả cho "/topic/reply"
    @MessageMapping("/message")
    @SendTo("/topic/reply")
    public String processMessageFromClient(String message) throws Exception {
        Thread.sleep(1000); // giả lập độ trễ xử lý
        return "Reply from server: " + HtmlUtils.htmlEscape(message);
    }
}