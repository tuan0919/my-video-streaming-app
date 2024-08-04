package com.nlu.app.service;

import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class FileService {
    @Value("${amazon.s3.bucket}") @NonFinal
    String bucket;
    @Value("${amazon.cloudfront.secret-key-location}") @NonFinal
    String secretKeyLocation;
    @Value("${amazon.cloudfront.public-key}") @NonFinal
    String keypairId;
    @Value("${amazon.cloudfront.url}") @NonFinal
    String cloudFrontUrl;
    S3Client s3Client;
    @NonFinal
    S3Presigner signer;

    @Autowired
    public void setSigner(S3Presigner signer) {
        this.signer = signer;
    }

    public String generateURL(String fileName) {
        CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
        Instant expirationDate = Instant.now().plus(1, ChronoUnit.MINUTES);
        String resource = String.format("%s/%s", cloudFrontUrl, fileName);
        CannedSignerRequest cannedRequest = null;
        try {
            cannedRequest = CannedSignerRequest.builder()
                    .resourceUrl(resource)
                    .privateKey(Path.of(secretKeyLocation))
                    .keyPairId(keypairId)
                    .expirationDate(expirationDate)
                    .build();
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedRequest);
        return signedUrl.url();
    }

    public String signUpload (String keyName) {
        String extension = keyName.substring(keyName.lastIndexOf(".") + 1);
        String key = rollKey(extension);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key("inventory/"+key)
                .build();
        PutObjectPresignRequest signRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(1))
                .putObjectRequest(objectRequest)
                .build();
        String url = signer.presignPutObject(signRequest).url().toString();
        signer.close();
        return url;
    }

    private String rollKey (String extension) {
        String key = UUID.randomUUID().toString()+"."+extension;
        while (true) {
            try {
                HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();
                HeadObjectResponse response = s3Client.headObject(headObjectRequest);
                log.info("Key already exists, generate a new one");
                key = UUID.randomUUID().toString()+"."+extension;
            } catch (Exception e) {
                log.info("Key does not exist, safe to use");
                break;
            }
        }
        return key;
    }

}
