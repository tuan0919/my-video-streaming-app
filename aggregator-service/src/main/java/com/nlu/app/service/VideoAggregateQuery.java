package com.nlu.app.service;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_VideoDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.exception.ServiceException;
import com.nlu.app.mapper.VideoAggregateMapper;
import com.nlu.app.repository.webclient.IdentityWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import com.nlu.app.repository.webclient.VideoStreamingWebClient;
import com.nlu.app.util.MyUtils;
import com.nlu.app.configuration.WebClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoAggregateQuery {
    private WebClient iWebClient;
    private WebClient pWebClient;
    private WebClient vWebClient;
    private final VideoAggregateMapper videoAggregateMapper;

    @Autowired
    private void setiWebClient(@Qualifier("identityWebClient") WebClient iWebClient) {
        this.iWebClient = iWebClient;
    }

    @Autowired
    private void setpWebClient(@Qualifier("profileWebClient") WebClient pWebClient) {
        this.pWebClient = pWebClient;
    }

    @Autowired
    private void setvWebClient(@Qualifier("videoStreamingWebClient") WebClient vWebClient) {
        this.vWebClient = vWebClient;
    }

    public Mono<ClientView_VideoDetailsDTO> getVideoDetails(String videoId, String userId, String username) {
        var identityWebClient = WebClientBuilder.createClient(iWebClient, IdentityWebClient.class);
        var profileWebClient = WebClientBuilder.createClient(pWebClient, ProfileWebClient.class);
        var videoStreamingWebClient = WebClientBuilder.createClient(vWebClient, VideoStreamingWebClient.class);

        return videoStreamingWebClient.getVideoDetails(videoId, userId, username)
                .flatMap(vResponse -> {
                    VideoDetailsResponse details = vResponse.getResult();
                    String videoOwnerId = details.getOwnerId();
                    return Mono.zip(identityWebClient.getUser(videoOwnerId),
                            profileWebClient.getProfile(videoOwnerId),
                            profileWebClient.checkUserFollowing(videoOwnerId, userId, username),
                            Mono.just(details));
                })
                .map(tuple -> {
                    UserResponse ownerIdentity = tuple.getT1().getResult();
                    ProfileResponseDTO ownerProfile = tuple.getT2().getResult();
                    Boolean isFollowing = tuple.getT3().getResult();
                    VideoDetailsResponse videoStat = tuple.getT4();
                    return videoAggregateMapper.mapToDTO(ownerIdentity, ownerProfile, isFollowing, videoStat);
                })
                .onErrorResume(e -> {
                    if (e instanceof Exception) {
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }
}
