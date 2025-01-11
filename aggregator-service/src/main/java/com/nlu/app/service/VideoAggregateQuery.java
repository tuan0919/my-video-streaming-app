package com.nlu.app.service;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_SearchVideoDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_VideoDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.exception.ServiceException;
import com.nlu.app.mapper.VideoAggregateMapper;
import com.nlu.app.common.share.webclient.IdentityWebClient;
import com.nlu.app.common.share.webclient.ProfileWebClient;
import com.nlu.app.common.share.webclient.VideoStreamingWebClient;
import com.nlu.app.util.MyUtils;
import com.nlu.app.configuration.WebClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public Mono<List<ClientView_VideoDetailsDTO>> getVideoFeed(String userId, String username, Integer page, Integer pageSize) {
        var videoStreamingWebClient = WebClientBuilder.createClient(vWebClient, VideoStreamingWebClient.class);
        return videoStreamingWebClient.getIds_SortByPoints(page, pageSize)
                .map(result -> result.getResult())
                .flatMap(ids -> {
                    List<Mono<ClientView_VideoDetailsDTO>> detailsMonos = ids.stream()
                            .map(videoId -> getVideoDetails(videoId, userId, username))  // Gọi getVideoDetails cho mỗi videoId
                            .collect(Collectors.toList());
                    return Mono.zip(detailsMonos, results -> Arrays.stream(results)
                            .map(result -> (ClientView_VideoDetailsDTO) result)
                            .collect(Collectors.toList()));
                })
                .switchIfEmpty(Mono.just(Arrays.asList()));
    }

    public Mono<List<ClientView_VideoDetailsDTO>> getVideoFeedExcludeId(String userId, String username, Integer page, Integer pageSize, String excludeId) {
        var videoStreamingWebClient = WebClientBuilder.createClient(vWebClient, VideoStreamingWebClient.class);
        return videoStreamingWebClient.getIds_SortByPointsExcludeId(page, pageSize, excludeId)
                .map(result -> result.getResult())
                .flatMap(ids -> {
                    List<Mono<ClientView_VideoDetailsDTO>> detailsMonos = ids.stream()
                            .map(videoId -> getVideoDetails(videoId, userId, username))  // Gọi getVideoDetails cho mỗi videoId
                            .collect(Collectors.toList());
                    return Mono.zip(detailsMonos, results -> Arrays.stream(results)
                            .map(result -> (ClientView_VideoDetailsDTO) result)
                            .collect(Collectors.toList()));
                })
                .switchIfEmpty(Mono.just(Arrays.asList())).onErrorResume(e -> {
                    if (e instanceof Exception) {
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }

    public Mono<List<ClientView_SearchVideoDTO>> searchVideoByTitle(Integer page, Integer pageSize, String title, String userId, String username) {
        var videoStreamingWebClient = WebClientBuilder.createClient(vWebClient, VideoStreamingWebClient.class);
        return videoStreamingWebClient.getIds_SearchByTitle(page, pageSize, title)
                .map(AppResponse::getResult)
                .flatMap(ids -> {
                    List<Mono<VideoDetailsResponse>> detailsMonos = ids.stream()
                            .map(videoId -> videoStreamingWebClient.getVideoDetails(videoId, userId, username)
                                    .map(AppResponse::getResult))
                            .collect(Collectors.toList());
                    return Mono.zip(detailsMonos, results -> Arrays.stream(results)
                            .map(r -> (VideoDetailsResponse) r)
                            .map(r -> videoAggregateMapper.mapToDTO(r))
                            .collect(Collectors.toList()));
                })
                .switchIfEmpty(Mono.just(Arrays.asList())).onErrorResume(e -> {
                    if (e instanceof Exception) {
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }
}
