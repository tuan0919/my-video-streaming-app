package com.nlu.app.service;

import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
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

    public CookiesForCustomPolicy signCookies() {
        CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
        CustomSignerRequest customRequest = null;
        Instant expirationDate = Instant.now().plus(5, ChronoUnit.MINUTES);
        try {
            customRequest = CustomSignerRequest.builder()
                    .resourceUrl(cloudFrontUrl+"/inventory/*")
                    .privateKey(Path.of(secretKeyLocation))
                    .keyPairId(keypairId)
                    .expirationDate(expirationDate)
                    .build();
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return cloudFrontUtilities.getCookiesForCustomPolicy(customRequest);
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

    @PreAuthorize("hasAnyRole('ADMIN')")
    public String moveToInventory(String oldKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key("temp/"+oldKey)
                    .build();
            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
        } catch (S3Exception  e) {
            e.printStackTrace();
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        String extension = oldKey.substring(oldKey.lastIndexOf(".") + 1);
        String newKey = rollKey(extension);
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .destinationBucket(bucket)
                .sourceKey("temp/"+oldKey)
                .destinationKey("inventory/"+newKey)
                .build();
        s3Client.copyObject(copyRequest);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key("temp/"+oldKey)
                .build();
        s3Client.deleteObject(deleteRequest);
        return "OK";
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    public String uploadToTemp (String key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key("temp/"+key)
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
