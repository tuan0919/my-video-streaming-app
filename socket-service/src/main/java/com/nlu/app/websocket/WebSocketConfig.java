package com.nlu.app.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt một message broker đơn giản với tiền tố "/topic"
        config.enableSimpleBroker("/topic");
        // Định nghĩa tiền tố "/app" cho các message hướng đến server
        config.setApplicationDestinationPrefixes("/app");
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint cho WebSocket với SockJS fallback
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        // Lấy thông tin userId từ header
                        String username = request.getHeaders().getFirst("X-Username");
                        System.out.println("Current websocket username: " + username);
                        if (username == null) {
                            response.setStatusCode(HttpStatus.FORBIDDEN); // Nếu không có userId, từ chối kết nối
                            return false;
                        }

                        // Lưu userId vào attributes để có thể truy cập sau này trong WebSocket session
                        attributes.put("userId", username);
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

                    }
                });
    }
}