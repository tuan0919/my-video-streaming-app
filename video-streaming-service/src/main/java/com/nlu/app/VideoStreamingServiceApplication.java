package com.nlu.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class VideoStreamingServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(VideoStreamingServiceApplication.class, args);
	}

}
