package com.nlu.app.repository.webclient;

import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.profile_service.response.ProfileCreationResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.dto.AppResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface ProfileWebClient {
    @GetExchange(url = "profile/users/get/{userId}")
    Mono<AppResponse<ProfileResponseDTO>> getProfile(@PathVariable String userId);
}
