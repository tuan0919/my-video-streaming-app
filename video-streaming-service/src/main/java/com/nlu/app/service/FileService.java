package com.nlu.app.service;

import com.nlu.app.dto.request.PutFileRequest;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.response.SaveFileResponse;
import com.nlu.app.dto.response.SignedURLResponse;
import com.nlu.app.dto.webclient.identity.request.TokenUserRequest;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.repository.IdentityWebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3AsyncClient;
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
    S3AsyncClient s3Client;
    IdentityWebClient identityWebClient;
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

    private String signedURL(String fileName) {
        CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
        Instant expirationDate = Instant.now().plus(6, ChronoUnit.MINUTES);
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

    public SignedURLResponse generateURL(String fileName) {
        String link = signedURL(fileName);
        return SignedURLResponse.builder().link(link).build();
    }

    @Cacheable(value = "resourceLinks", key = "#key")
    public String generateResourceURL (String key) {
        return signedURL(key);
    }

    public SaveFileResponse moveToInventory(SaveFileRequest request, String userId, String username) {
        String oldKey = request.getFilename();
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key("temp/"+username+"/"+oldKey)
                    .build();
            s3Client.headObject(headRequest);
            String extension = oldKey.substring(oldKey.lastIndexOf(".") + 1);
            String newKey = UUID.randomUUID().toString() + "." + extension;
            String resultKey = "inventory/"+username+"/"+newKey;
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .destinationBucket(bucket)
                    .sourceKey("temp/"+username+"/"+oldKey)
                    .destinationKey(resultKey)
                    .build();
            s3Client.copyObject(copyRequest);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key("temp/"+username+"/"+oldKey)
                    .build();
            return SaveFileResponse.builder()
                    .key(resultKey)
                    .build();
        } catch (S3Exception e) {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    public SignedURLResponse uploadToTemp (PutFileRequest request, String userId, String username) {
        String fileName = request.getFilename();
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key("temp/"+username+"/"+fileName)
                .build();
        PutObjectPresignRequest signRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();
        var response = signer.presignPutObject(signRequest);
        signer.close();
        String signedLink = response.url().toString();
        return SignedURLResponse.builder().link(signedLink).build();
    }
}
