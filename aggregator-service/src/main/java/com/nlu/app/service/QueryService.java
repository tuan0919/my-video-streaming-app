package com.nlu.app.service;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserDetailsDTO;
import com.nlu.app.common.share.dto.identity_service.response.RoleResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.repository.webclient.IdentityWebClient;
import com.nlu.app.repository.webclient.ProfileWebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class QueryService {
    ProfileWebClient profileWebClient;
    IdentityWebClient identityWebClient;

    public Mono<ClientView_UserDetailsDTO> queryUserDetails(String userId) {
        Mono<UserResponse> userMono = identityWebClient.getUser(userId).map(rs -> rs.getResult());
        Mono<ProfileResponseDTO> profileMono = profileWebClient.getProfile(userId).map(rs -> rs.getResult());
        return Mono.zip(userMono, profileMono)
                .map(tuple -> {
                    UserResponse userResponse = tuple.getT1();
                    ProfileResponseDTO profileResponse = tuple.getT2();
                    // merge
                    return ClientView_UserDetailsDTO.builder()
                            .userId(userResponse.getId())
                            .bio(profileResponse.getBio())
                            .avatarLink(profileResponse.getAvatar()+"_LINK")
                            .fullName(profileResponse.getBio())
                            .region(profileResponse.getCountry())
                            .username(userResponse.getUsername())
                            .role(userResponse.getRoles().stream().map(RoleResponse::getName).toList())
                            .email(userResponse.getEmail())
                            .build();
                }).onErrorResume(e -> Mono.error(e));
    }
}
