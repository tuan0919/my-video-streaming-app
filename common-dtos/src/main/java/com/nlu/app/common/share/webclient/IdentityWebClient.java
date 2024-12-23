package com.nlu.app.common.share.webclient;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface IdentityWebClient {
    @GetExchange(url = "identity/users/get/{userId}")
    Mono<AppResponse<UserResponse>> getUser(@PathVariable String userId);
    @PostExchange("identity/users/query/map-by-ids")
    Mono<AppResponse<Map<String, UserResponse>>> getUsersAndMapByIds(@RequestBody List<String> userIds);
}
