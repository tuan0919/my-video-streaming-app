package com.nlu.app.common.share.webclient;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.profile_service.request.SaveVideoRequest;
import com.nlu.app.common.share.dto.profile_service.response.ProfileFollowStatusResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ProfileWebClient {
    @GetExchange(url = "profile/users/get/{userId}")
    Mono<AppResponse<ProfileResponseDTO>> getProfile(@PathVariable String userId);
    @GetExchange(url = "profile/users/get/follow/{userId}")
    Mono<AppResponse<ProfileFollowStatusResponse>> getFollowStatus(@PathVariable String userId);
    @GetExchange("profile/users/following/{followId}")
    Mono<AppResponse<Boolean>> checkUserFollowing(@PathVariable String followId,
                                                  @RequestHeader("X-UserId") String userId,
                                                  @RequestHeader("X-Username") String username);
    @PostExchange("profile/users/query")
    Mono<AppResponse<Map<String, ProfileResponseDTO>>> getProfileMapByUserId(@RequestBody List<String> userIds);
    @PostExchange("profile/users/save/video")
    Mono<AppResponse<String>> saveVideo(@RequestHeader("X-UserId") String userId,
                                                                 @RequestBody SaveVideoRequest request);
    @GetExchange("profile/users/save/video")
    Mono<AppResponse<List<String>>> getSavedVideoIds(@RequestHeader("X-UserId") String userId,
                                                     @RequestParam("page") Integer page,
                                                     @RequestParam("pageSize") Integer pageSize);
}
