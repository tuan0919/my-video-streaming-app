package com.nlu.app.service;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.RoleResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.exception.ServiceException;
import com.nlu.app.repository.webclient.IdentityWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import com.nlu.app.util.MyUtils;
import com.nlu.app.configuration.WebClientBuilder;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UserAggregateQuery {
    WebClient iWebClient;
    WebClient pWebClient;

    @Autowired
    public void setiWebClient(@Qualifier("identityWebClient") WebClient iWebClient) {
        this.iWebClient = iWebClient;
    }

    @Autowired
    public void setpWebClient(@Qualifier("profileWebClient") WebClient pWebClient) {
        this.pWebClient = pWebClient;
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
}
