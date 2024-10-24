package com.nlu.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SNSNotificationController {
    ObjectMapper objectMapper;
    @PostMapping(value = "/sns-notification", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> receiveNotification(
            @RequestBody String payload,  // Nhận payload dưới dạng String
            @RequestHeader(value = "x-amz-sns-message-type") String messageType) throws JsonProcessingException {

        log.info("Received SNS message type: {}", messageType);

        // Chuyển đổi payload thành JsonNode
        JsonNode jsonNode;
        jsonNode = objectMapper.readTree(payload);
        // Xử lý SubscriptionConfirmation từ SNS
        if ("SubscriptionConfirmation".equals(messageType)) {
            String subscribeURL = jsonNode.get("SubscribeURL").asText();
            log.info("Confirming subscription URL: {}", subscribeURL);

            // Gửi GET request đến subscribeURL để xác nhận
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(subscribeURL, String.class);
            log.info("Subscription confirmed");
        }
        // Xử lý Notification từ SNS (khi có file được tải lên S3)
        else if ("Notification".equals(messageType)) {
            String message = jsonNode.get("Message").asText();
            log.info("Received notification message: {}", message);
            // Chuyển đổi message thành JsonNode
            JsonNode messageNode;
            messageNode = objectMapper.readTree(message);  // Chuyển đổi message sang JsonNode
            if (messageNode != null && messageNode.has("Records")) {
                JsonNode s3Object = messageNode.get("Records").get(0).get("s3").get("object");
                String s3Key = s3Object.get("key").asText();
                log.info("S3 object key: {}", s3Key);
            }
        }

        return ResponseEntity.ok("SNS message processed");
    }
}