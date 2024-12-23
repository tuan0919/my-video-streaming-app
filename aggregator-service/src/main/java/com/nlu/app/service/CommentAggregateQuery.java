package com.nlu.app.service;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_CommentDTO;
import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
import com.nlu.app.common.share.webclient.ProfileWebClient;
import com.nlu.app.exception.ServiceException;
import com.nlu.app.mapper.CommentAggregateMapper;
import com.nlu.app.common.share.webclient.CommentWebClient;
import com.nlu.app.common.share.webclient.IdentityWebClient;
import com.nlu.app.util.MyUtils;
import com.nlu.app.configuration.WebClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentAggregateQuery {
    private WebClient iWebClient;
    private WebClient pWebClient;
    private WebClient cWebClient;
    private final CommentAggregateMapper commentAggregateMapper;

    @Autowired
    private void setiWebClient(@Qualifier("identityWebClient") WebClient iWebClient) {
        this.iWebClient = iWebClient;
    }

    @Autowired
    private void setpWebClient(@Qualifier("profileWebClient") WebClient pWebClient) {
        this.pWebClient = pWebClient;
    }

    @Autowired
    public void setcWebClient(@Qualifier("commentWebClient") WebClient cWebClient) {
        this.cWebClient = cWebClient;
    }

    public Mono<List<ClientView_CommentDTO>> getReplyComments(String commentId, String userId, String username) {
        var identityWebClient = WebClientBuilder.createClient(iWebClient, IdentityWebClient.class);
        var commentWebClient = WebClientBuilder.createClient(cWebClient, CommentWebClient.class);
        var profileWebClient = WebClientBuilder.createClient(pWebClient, ProfileWebClient.class);
        return commentWebClient.getCommentsReplied(commentId, userId, username)
                .flatMap(response -> {
                    var commentIds = response.getResult().stream().map(CommentResponse::getId).toList();
                    var ownerIds = response.getResult().stream().map(CommentResponse::getUserId).toList();
                    return Mono.zip(identityWebClient.getUsersAndMapByIds(ownerIds)
                            , profileWebClient.getProfileMapByUserId(ownerIds)
                            , commentWebClient.getUserReactionsForComments(commentIds, userId, username)
                            , Mono.just(response));
                }).map(tuple -> {
                    var userId_identityResponse = tuple.getT1().getResult();
                    var profileMap = tuple.getT2().getResult();
                    var commentId_action = tuple.getT3().getResult();
                    var listCommentsResponses = tuple.getT4().getResult();
                    return listCommentsResponses.stream()
                            .map(comment -> {
                                String ownerId = comment.getUserId();
                                String cId = comment.getId();
                                return commentAggregateMapper.toDTO(comment,
                                        userId_identityResponse.get(ownerId),
                                        profileMap.get(ownerId),
                                        commentId_action.get(cId));
                            }).toList();
                }).onErrorResume(e -> {
                    if (e instanceof Exception) {
                        e.printStackTrace();
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }

    public Mono<List<ClientView_CommentDTO>> getCommentsByVideoId(String videoId, String userId, String username) {
        var identityWebClient = WebClientBuilder.createClient(iWebClient, IdentityWebClient.class);
        var commentWebClient = WebClientBuilder.createClient(cWebClient, CommentWebClient.class);
        var profileWebClient = WebClientBuilder.createClient(pWebClient, ProfileWebClient.class);
        return commentWebClient.getCommentsByVideoId(videoId, userId, username)
                .flatMap(response -> {
                    var commentIds = response.getResult().stream().map(CommentResponse::getId).toList();
                    var ownerIds = response.getResult().stream().map(CommentResponse::getUserId).toList();
                    return Mono.zip(identityWebClient.getUsersAndMapByIds(ownerIds)
                    , profileWebClient.getProfileMapByUserId(ownerIds)
                    , commentWebClient.getUserReactionsForComments(commentIds, userId, username)
                    , Mono.just(response));
                }).map(tuple -> {
                    var userId_identityResponse = tuple.getT1().getResult();
                    var profileMap = tuple.getT2().getResult();
                    var commentId_action = tuple.getT3().getResult();
                    var listCommentsResponses = tuple.getT4().getResult();
                    return listCommentsResponses.stream()
                            .map(comment -> {
                                String ownerId = comment.getUserId();
                                String commentId = comment.getId();
                                return commentAggregateMapper.toDTO(comment,
                                        userId_identityResponse.get(ownerId),
                                        profileMap.get(ownerId),
                                        commentId_action.get(commentId));
                            }).toList();
                }).onErrorResume(e -> {
                    if (e instanceof Exception) {
                        e.printStackTrace();
                        ServiceException exception = MyUtils.convertException((Exception) e);
                        return Mono.error(exception);
                    }
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }
}
