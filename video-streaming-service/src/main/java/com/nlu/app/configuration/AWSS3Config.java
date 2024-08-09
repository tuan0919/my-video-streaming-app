package com.nlu.app.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AWSS3Config {
    @Value("${amazon.aws.accessKey}")
    String accessKey;
    @Value("${amazon.aws.secretKey}")
    String secretKey;
    @Value("${amazon.aws.region}")
    String region;

    @Bean
    public S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("nqat0919-uploader"))
                .build();
    }

    @Bean
    public S3AsyncClient getS3AsyncClient() {
        return S3AsyncClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("nqat0919-uploader"))
                .build();
    }

    @Bean
    @Scope("prototype")
    public S3Presigner getS3Presigner() {
        return S3Presigner.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("nqat0919-uploader"))
                .build();
    }
}
