package com.nlu.app.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSS3Config {
    @Value("${amazon.aws.accessKey}")
    String accessKey;
    @Value("${amazon.aws.secretKey}")
    String secretKey;
    @Value("${amazon.aws.region}")
    String region;

}
