package com.nlu.app.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Properties;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic user_created() {
        return TopicBuilder.name("user_created")
                .partitions(5)
                .replicas(3) // Number of replicas
                .build();
    }

    @Bean
    public NewTopic comment_replied() {
        return TopicBuilder.name("comment_replied")
                .partitions(5)
                .replicas(3) // Number of replicas
                .build();
    }

    @Bean
    public NewTopic notification_created() {
        return TopicBuilder.name("notification_created")
                .partitions(5)
                .replicas(3) // Number of replicas
                .build();
    }

    @Bean
    public NewTopic dlq_user_created() {
        return TopicBuilder.name("dlq_user_created")
                .partitions(5)
                .replicas(3) // Number of replicas
                .build();
    }
}
