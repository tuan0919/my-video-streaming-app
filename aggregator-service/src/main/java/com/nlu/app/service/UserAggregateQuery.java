package com.nlu.app.service;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_NotificationDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_SearchUserDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserDetailsDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserPageDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.RoleResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserProfileResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.notification_service.response.NotificationResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileFollowStatusResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoCountsResponse;
import com.nlu.app.common.share.webclient.*;
import com.nlu.app.exception.ServiceException;
import com.nlu.app.mapper.NotificationAggregateMapper;
import com.nlu.app.mapper.UserAggregateMapper;
import com.nlu.app.util.MyUtils;
import com.nlu.app.configuration.WebClientBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequiredArgsConstructor
public class UserAggregateQuery {
    WebClient iWebClient;
    WebClient pWebClient;
    WebClient nWebClient;
    WebClient cWebClient;
    WebClient vWebClient;
    private final NotificationAggregateMapper notificationAggregateMapper;
    private UserAggregateMapper userAggregateMapper;

    @Autowired
    private void setiWebClient(@Qualifier("identityWebClient") WebClient iWebClient) {
        this.iWebClient = iWebClient;
    }

    @Autowired
    private void setpWebClient(@Qualifier("profileWebClient") WebClient pWebClient) {
        this.pWebClient = pWebClient;
    }

    @Autowired
    private void setnWebClient(@Qualifier("notificationWebClient") WebClient nWebClient) {
        this.nWebClient = nWebClient;
    }

    @Autowired
    private void setvWebClient(@Qualifier("videoStreamingWebClient") WebClient vWebClient) {
        this.vWebClient = vWebClient;
    }

    @Autowired
    private void setcWebClient(@Qualifier("commentWebClient") WebClient cWebClient) {
        this.cWebClient = cWebClient;
    }

    public Mono<ClientView_UserDetailsDTO> queryUserDetails(String userId) {
        var identityWebClient = WebClientBuilder.createClient(iWebClient, IdentityWebClient.class);
        var profileWebClient = WebClientBuilder.createClient(pWebClient, ProfileWebClient.class);
        Mono<AppResponse<UserResponse>> userMono = identityWebClient
                .getUser(userId);
        Mono<AppResponse<ProfileResponseDTO>> profileMono = profileWebClient.getProfile(userId);
        // Thực hiện query song song cả hai request rồi sau đó zip lại
        return Mono.zip(userMono, profileMono)
                .map(tuple -> {
                    UserResponse userResponse = tuple.getT1().getResult();
                    ProfileResponseDTO profileResponse = tuple.getT2().getResult();
                    // gộp kết quả của cả 2 response
                    return ClientView_UserDetailsDTO.builder()
                            .userId(userResponse.getId())
                            .bio(profileResponse.getBio())
                            .avatarLink("https://www.strasys.uk/wp-content/uploads/2022/02/Depositphotos_484354208_S.jpg")
                            .fullName(profileResponse.getBio())
                            .region(profileResponse.getCountry())
                            .username(userResponse.getUsername())
                            .role(userResponse.getRoles().stream().map(RoleResponse::getName).toList())
                            .email(userResponse.getEmail())
                            .build();
                }).onErrorResume(e -> {
                    if (e instanceof Exception) {
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }

    public Mono<ClientView_UserPageDetailsDTO> queryUserPageDetails(String targetId, boolean isMySelf, String userId, String username) {
        var videoStreamingWebClient = WebClientBuilder.createClient(vWebClient, VideoStreamingWebClient.class);
        var profileWebClient = WebClientBuilder.createClient(pWebClient, ProfileWebClient.class);
        var identityWebClient = WebClientBuilder.createClient(iWebClient, IdentityWebClient.class);
        Mono<UserResponse> monoIdentity = identityWebClient.getUser(targetId).map(AppResponse::getResult);
        Mono<ProfileResponseDTO> monoProfile = profileWebClient.getProfile(targetId).map(AppResponse::getResult);
        Mono<ProfileFollowStatusResponse> monoProfileFollowStats = profileWebClient.getFollowStatus(targetId).map(AppResponse::getResult);
        Mono<VideoCountsResponse> monoVideoCount = videoStreamingWebClient.getVideoCounts(targetId).map(AppResponse::getResult);
        Mono<Boolean> monoIsFollow;
        if (!isMySelf) {
            monoIsFollow = profileWebClient.checkUserFollowing(targetId, userId, username).map(AppResponse::getResult);
        } else {
            monoIsFollow = Mono.just(false);
        }
        return Mono.zip(monoIdentity, monoProfile, monoProfileFollowStats, monoVideoCount, monoIsFollow)
                .map(tuple -> {
                    var identity = tuple.getT1();
                    var profile = tuple.getT2();
                    var stats = tuple.getT3();
                    var count = tuple.getT4();
                    var isFollow = tuple.getT5();
                    return userAggregateMapper.mapToDTO(identity, profile, stats, count, isMySelf, isFollow);
                }).onErrorResume(e -> {
                    if (e instanceof Exception) {
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }

    public Mono<List<ClientView_NotificationDTO>> queryNotifications(String userId, String username, Integer page, Integer pageSize) {
        var notificationWebClient = WebClientBuilder.createClient(nWebClient, NotificationWebClient.class);
        return notificationWebClient.getNotifications(userId, page, pageSize)
                .flatMapMany(response -> Flux.fromIterable(response.getResult()))
                .flatMap(notify -> {
                    Mono<String> videoThumbnailMono = getVideoThumbnail(notify, userId, username);
                    Mono<String> avatarMono = getAvatar(notify.getRelatedObjectId(), notify.getRelatedEvent());
                    Mono<String> routeObjectMono = getRouteObjectId(notify.getRelatedObjectId(), notify.getRelatedEvent());
                    return Mono.zip(videoThumbnailMono, avatarMono, routeObjectMono)
                            .map(tuple -> {
                                String videoThumbnail = tuple.getT1();
                                String avatar = tuple.getT2();
                                String routeObjectId = tuple.getT3();
                                return notificationAggregateMapper.mapToDTO(notify, videoThumbnail, routeObjectId, avatar);
                            });
                })
                .collectList()
                .onErrorResume(e -> {
                    if (e instanceof Exception) {
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }

    public Mono<List<ClientView_SearchUserDTO>> searchUserByUsername(Integer page, Integer pageSize, String username) {
        var identityWebClient = WebClientBuilder.createClient(iWebClient, IdentityWebClient.class);
        var profileWebClient = WebClientBuilder.createClient(pWebClient, ProfileWebClient.class);
        return identityWebClient.getUserIds_SearchByUsername(page, pageSize, username)
                .flatMapMany(response -> Flux.fromIterable(response.getResult()))
                .flatMap(id -> {
                    Mono<UserResponse> userResponseMono = identityWebClient.getUser(id)
                            .map(AppResponse::getResult);
                    Mono<ProfileResponseDTO> userProfileResponseMono = profileWebClient.getProfile(id)
                            .map(AppResponse::getResult);
                    Mono<ProfileFollowStatusResponse> statusResponseMono = profileWebClient.getFollowStatus(id)
                            .map(AppResponse::getResult);
                    return Mono.zip(userResponseMono, userProfileResponseMono, statusResponseMono)
                            .map(tuple -> {
                                var userResponse = tuple.getT1();
                                var profileResponse = tuple.getT2();
                                var statResponse = tuple.getT3();
                                return userAggregateMapper.toDTO(userResponse, profileResponse, statResponse);
                            });
                })
                .collectList()
                .onErrorResume(e -> {
                    if (e instanceof Exception) {
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }

    private Mono<String> getVideoThumbnail(NotificationResponse notification, String userId, String username) {
        var commentWebClient = WebClientBuilder.createClient(cWebClient, CommentWebClient.class);
        var videoWebClient = WebClientBuilder.createClient(vWebClient, VideoStreamingWebClient.class);
        switch (notification.getRelatedEvent()) {
            case "COMMENT_REPLY_EVENT" -> {
                // lấy thông tin comment
                return commentWebClient.getCommentById(notification.getRelatedObjectId())
                        .map(res -> res.getResult().getVideoId()) // lấy videoId
                        .flatMap(videoId -> {
                            // lấy video thumbnail
                            return videoWebClient.getVideoDetails(videoId, userId, username);
                        }).map(details -> details.getResult().getThumbnail());
            }
        }
        return Mono.just("https://minecraft.wiki/images/thumb/New_Options_Screen.png/1200px-New_Options_Screen.png?1bde7");
    }

    private Mono<String> getAvatar(String relatedObjectId, String action) {
        var commentWebClient = WebClientBuilder.createClient(cWebClient, CommentWebClient.class);
        var profileWebClient = WebClientBuilder.createClient(pWebClient, ProfileWebClient.class);
        switch (action) {
            case "NEW_VIDEO_CREATED_EVENT" -> {
                // TODO: query thông tin avatar của chủ video
                Mono.just("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSwdIVSqaMsmZyDbr9mDPk06Nss404fosHjLg&s");
            }
            case "COMMENT_REPLY_EVENT" -> {
                // lấy thông tin comment
                // sau đó lấy userId
                return commentWebClient.getCommentById(relatedObjectId)
                        .map(res -> res.getResult().getUserId())
                        .flatMap(userId -> profileWebClient.getProfile(userId))
                        .map(res -> res.getResult().getAvatar());
            }
            case "NEW_USER_CREATED_EVENT" -> {
                // show hình welcome mặc định của hệ thống
                Mono.just("https://t4.ftcdn.net/jpg/03/75/58/15/360_F_375581544_r5aDqkhnhrqy15AdaqKXUI30MMtL8IIq.jpg");
            }
        }
        return Mono.just("https://img.freepik.com/premium-vector/default-image-icon-vector-missing-picture-page-website-design-mobile-app-no-photo-available_87543-11093.jpg");
    }

    private Mono<String> getRouteObjectId(String relatedObjectId, String action) {
        var commentWebClient = WebClientBuilder.createClient(cWebClient, CommentWebClient.class);
        switch (action) {
            case "NEW_VIDEO_CREATED_EVENT" -> {
                return Mono.just(relatedObjectId);
            }
            case "COMMENT_REPLY_EVENT" -> {
                // lấy thông tin video của comment đc reply
                // sau đó lấy videoId
                return commentWebClient.getCommentById(relatedObjectId)
                        .map(res -> res.getResult().getVideoId());
            }
            case "NEW_USER_CREATED_EVENT" -> {
                // trả về userId
                return Mono.just(relatedObjectId);
            }
        }
        return Mono.empty();
    }

    @Autowired
    public void setUserAggregateMapper(UserAggregateMapper userAggregateMapper) {
        this.userAggregateMapper = userAggregateMapper;
    }
}
