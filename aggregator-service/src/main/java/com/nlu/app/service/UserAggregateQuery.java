package com.nlu.app.service;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_NotificationDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.RoleResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.exception.ServiceException;
import com.nlu.app.mapper.NotificationAggregateMapper;
import com.nlu.app.common.share.webclient.IdentityWebClient;
import com.nlu.app.common.share.webclient.NotificationWebClient;
import com.nlu.app.common.share.webclient.ProfileWebClient;
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
    private final NotificationAggregateMapper notificationAggregateMapper;

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

    public Mono<List<ClientView_NotificationDTO>> queryNotifications(String userId, Integer page, Integer pageSize) {
        var notificationWebClient = WebClientBuilder.createClient(nWebClient, NotificationWebClient.class);
        return notificationWebClient.getNotifications(userId, page, pageSize)
                .map(response -> {
                    var notifications = response.getResult();
                    return notifications.stream()
                            .map(notify -> {
                                String videoThumbnail = getVideoThumbnail(notify.getRelatedObjectId());
                                String avatar = getAvatar(notify.getRelatedObjectId(), notify.getRelatedEvent());
                                String href = getHref(notify.getRelatedObjectId(), notify.getRelatedEvent());
                                return notificationAggregateMapper.mapToDTO(notify, videoThumbnail, href, avatar);
                            }).toList();
                }).onErrorResume(e -> {
                    if (e instanceof Exception) {
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }

    private String getVideoThumbnail(String relatedObjectId) {
        // TODO: làm gì đó tại đây để lấy thông tin thumbnail video...
        return "https://minecraft.wiki/images/thumb/New_Options_Screen.png/1200px-New_Options_Screen.png?1bde7";
    }

    private String getAvatar(String relatedObjectId, String action) {
        // TODO: làm gì đó tại đây để lấy thông tin avatar cho notification này.
        switch (action) {
            case "NEW_VIDEO_CREATED_EVENT" -> {
                // TODO: query thông tin avatar của chủ video
                return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSwdIVSqaMsmZyDbr9mDPk06Nss404fosHjLg&s";
            }
            case "COMMENT_REPLY_EVENT" -> {
                // TODO: query thông tin avatar của user đã reply comment
                return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSwdIVSqaMsmZyDbr9mDPk06Nss404fosHjLg&s";
            }
            case "NEW_USER_CREATED_EVENT" -> {
                // show hình welcome mặc định của hệ thống
                return  "https://t4.ftcdn.net/jpg/03/75/58/15/360_F_375581544_r5aDqkhnhrqy15AdaqKXUI30MMtL8IIq.jpg";
            }
        }
        return "https://img.freepik.com/premium-vector/default-image-icon-vector-missing-picture-page-website-design-mobile-app-no-photo-available_87543-11093.jpg";
    }

    private String getHref(String relatedObjectId, String action) {
        switch (action) {
            case "NEW_VIDEO_CREATED_EVENT" -> {
                return "/video/"+relatedObjectId;
            }
            case "COMMENT_REPLY_EVENT" -> {
                // TODO: query thông tin avatar của user đã reply comment
                return "/comment/"+relatedObjectId;
            }
            case "NEW_USER_CREATED_EVENT" -> {
                return "/user/"+relatedObjectId;
            }
        }
        return null;
    }
}
