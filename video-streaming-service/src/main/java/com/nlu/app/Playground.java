package com.nlu.app;

import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Playground {
    public static void main(String[] args) throws Exception {
        CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
        Instant expirationDate = Instant.now().plus(30, ChronoUnit.SECONDS);
        String resourceUrl = "https://d1jlaprzsrhy1t.cloudfront.net/444490094_3374843149491960_8573212762319914279_n.jpg";
        String keyPairId = "K346G69MH91HZD";
        CannedSignerRequest cannedRequest = CannedSignerRequest.builder()
                .resourceUrl(resourceUrl)
                .privateKey(new java.io.File("C:\\Users\\Tuan\\Downloads\\pk-APKAZQ3DSOCMQW7M25AX.pem").toPath())
                .keyPairId(keyPairId)
                .expirationDate(expirationDate)
                .build();
        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedRequest);
        String url = signedUrl.url();
        System.out.println(url);

    }
}
