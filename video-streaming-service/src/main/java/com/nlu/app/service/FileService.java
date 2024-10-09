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
import org.springframework.security.access.prepost.PreAuthorize;
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

    public Mono<SignedURLResponse> generateURL(String fileName) {
        return Mono.fromCallable(() -> signedURL(fileName))
                .map(link -> SignedURLResponse.builder().link(link).build())
                .onErrorResume(error -> Mono.error(error))
                .subscribeOn(Schedulers.immediate());
    }

    @Cacheable(value = "resourceLinks", key = "#key")
    public String generateResourceURL (String key) {
        return signedURL(key);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<SaveFileResponse> moveToInventory(SaveFileRequest request, String token) {
        String oldKey = request.getFilename();
        var userTokenRequest = TokenUserRequest.builder()
                .token(token)
                .build();
        var wrap = new Object() {
            String userId;
        };
        var resultKey = new Object() {
            String value;
        };
        return identityWebClient.userInfo(userTokenRequest)
                .flatMap(response -> {
                    wrap.userId = response.getResult().getUserId();
                    log.info("temp resource: {}", "temp/"+wrap.userId+"/"+oldKey);
                    HeadObjectRequest headRequest = HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key("temp/"+wrap.userId+"/"+oldKey)
                            .build();
                    return Mono.fromFuture(s3Client.headObject(headRequest));
                })
                .onErrorResume(e -> {
                    if (e instanceof S3Exception castedEx) {
                        log.info("AWS Exception status: {}", castedEx.statusCode());
                        log.info("AWS Exception: {}", castedEx.getMessage());
                        return Mono.error(new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND));
                    }
                    else {
                        log.error("Unknown error happened: {}", e.getMessage());
                        return Mono.error(e);
                    }
                })
                .then(Mono.defer(() -> {
                    String extension = oldKey.substring(oldKey.lastIndexOf(".") + 1);
                    return rollKey(extension);
                }))
                .flatMap(newKey -> {
                    return Mono.fromFuture(() -> {
                        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                                .sourceBucket(bucket)
                                .destinationBucket(bucket)
                                .sourceKey("temp/"+wrap.userId+"/"+oldKey)
                                .destinationKey("inventory/"+wrap.userId+"/"+newKey)
                                .build();
                        resultKey.value = "inventory/"+wrap.userId+"/"+newKey;
                        return s3Client.copyObject(copyRequest);
                    });
                })
                .then(Mono.fromFuture(() -> {
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key("temp/"+wrap.userId+"/"+oldKey)
                            .build();
                    return s3Client.deleteObject(deleteRequest);
                }))
                .map(_ -> SaveFileResponse.builder()
                        .key(resultKey.value)
                        .build())
                .onErrorResume(error -> {
                    log.error("Unknown error happened: {}", error.getMessage());
                    return Mono.error(error);
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<SignedURLResponse> uploadToTemp (PutFileRequest request, String token) {
        String fileName = request.getFilename();
        var userTokenRequest = TokenUserRequest.builder()
                .token(token)
                .build();
        return identityWebClient.userInfo(userTokenRequest)
                .flatMap(response -> {
                    String userId = response.getResult().getUserId();
                    PutObjectRequest objectRequest = PutObjectRequest.builder()
                            .bucket(bucket)
                            .key("temp/"+userId+"/"+fileName)
                            .build();
                    PutObjectPresignRequest signRequest = PutObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(10))
                            .putObjectRequest(objectRequest)
                            .build();
                    return Mono.fromCallable(() -> signer.presignPutObject(signRequest))
                            .subscribeOn(Schedulers.immediate());
                })
                .map(response -> {
                    signer.close();
                    String signedLink = response.url().toString();
                    return SignedURLResponse.builder().link(signedLink).build();
                }).onErrorResume(error -> {
                    log.error("Unknown error happen here: {}", error.getMessage());
                    return Mono.error(error);
                });
    }

    private Mono<Boolean> doesKeyExists(String key) {
        return Mono.fromFuture(() -> {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return s3Client.headObject(headObjectRequest);
        })
        .map(_ -> true)
        .onErrorResume(error -> {
            if (error instanceof S3Exception) {
                log.info("Key {} is not existed, safe to use", key);
                return Mono.just(false);
            }
            else {
                log.info("Unknown error happen at here: {}", error.getMessage());
                return Mono.error(new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION));
            }
        });
    };

    private Mono<String> rollKey (String extension) {
        String key = UUID.randomUUID().toString() + "." + extension;
        return Mono.defer(() -> {
                return doesKeyExists(key);
            }).flatMap(isExist -> {
                if (isExist) return rollKey(extension);
                else
                    return Mono.just(key);
        });
    };
}
