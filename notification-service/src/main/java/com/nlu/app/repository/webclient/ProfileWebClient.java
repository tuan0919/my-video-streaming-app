package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.profile_service.response.FollowerUserIdsResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface ProfileWebClient {
    @GetExchange(url = "profile/users/follow?id={userId}")
    Mono<AppResponse<FollowerUserIdsResponse>> getFollowerIds(@PathVariable String userId);
}
