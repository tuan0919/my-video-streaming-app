package com.nlu.app.controller;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.profile_service.request.*;
import com.nlu.app.common.share.dto.profile_service.response.*;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.ICompensationService;
import com.nlu.app.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileController {
    ProfileService profileService;
    ICompensationService compensationService;

    @PostMapping("/internal/saga")
    public AppResponse<String> requestSaga(@RequestBody SagaAdvancedRequest sagaRequest) {
        return AppResponse.<String>builder()
                .result(profileService.sagaRequest(sagaRequest))
                .build();
    }

    @PostMapping
    public AppResponse<String> requestSaga(@RequestBody ProfileCreationRequest request) {
        return AppResponse.<String>builder()
                .result(profileService.create(request))
                .build();
    }

    @PutMapping("/follow")
    public AppResponse<String> follow(@RequestBody FollowRequest request,
                                      @RequestHeader("X-UserId") String userId,
                                      @RequestHeader("X-Username") String username) {
        return AppResponse.<String>builder()
                .result(profileService.follow(request, userId))
                .build();
    }

    @GetMapping("/follow")
    public AppResponse<FollowerUserIdsResponse> getFollowerIds(@RequestParam String id) {
        return AppResponse.<FollowerUserIdsResponse>builder()
                .result(profileService.getFollowerIds(id))
                .build();
    }

    @GetMapping("/following/{followId}")
    public AppResponse<Boolean> getFollowerIds(@PathVariable String followId,
                                               @RequestHeader("X-UserId") String userId,
                                               @RequestHeader("X-Username") String username) {
        return AppResponse.<Boolean>builder()
                .result(profileService.checkUserFollow(userId, followId, username))
                .build();
    }

    @GetMapping("/get/{userId}")
    public AppResponse<ProfileResponseDTO> getUserProfile(@PathVariable String userId) {
        return AppResponse.<ProfileResponseDTO>builder()
                .result(profileService.getUserProfile(userId))
                .build();
    }

    @GetMapping("/get/follow/{userId}")
    public AppResponse<ProfileFollowStatusResponse> getFollowStats(@PathVariable String userId) {
        return AppResponse.<ProfileFollowStatusResponse>builder()
                .result(profileService.getFollowStatus(userId))
                .build();
    }

    @PostMapping("/query")
    public AppResponse<Map<String, ProfileResponseDTO>> getUserProfiles(@RequestBody List<String> userIds) {
        return AppResponse.<Map<String, ProfileResponseDTO>>builder()
                .result(profileService.getUserProfile(userIds))
                .build();
    }

    @PutMapping("/get/avatar")
    public AppResponse<GetLinkUploadAvatarResponse> getLinkUploadAvatar(@RequestBody GetLinkUploadAvatarRequest request,
                                                                        @RequestHeader("X-UserId") String userId,
                                                                        @RequestHeader("X-Username") String username) {
        return AppResponse.<GetLinkUploadAvatarResponse>builder()
                .result(profileService.getLinkForUpload(request, userId, username))
                .build();
    }

    @PutMapping("/avatar")
    public AppResponse<ChangeAvatarResponse> changeAvatar(@RequestBody ChangeAvatarRequest request,
                                                          @RequestHeader("X-UserId") String userId,
                                                          @RequestHeader("X-Username") String username) {
        return AppResponse.<ChangeAvatarResponse>builder()
                .result(profileService.changeAvatar(request, userId, username))
                .build();
    }

    @PutMapping("/info")
    public AppResponse<String> updateProfile(@RequestBody UpdateProfileRequest request,
                                                           @RequestHeader("X-UserId") String userId,
                                                           @RequestHeader("X-Username") String username) {
        profileService.update(userId, request);
        return AppResponse.<String>builder()
                .result("OK")
                .build();
    }

    @PostMapping("/save/video")
    public AppResponse<String> saveVideo(@RequestHeader("X-UserId") String userId,
                                         @RequestBody SaveVideoRequest request) {
        return AppResponse.<String>builder()
                .result(profileService.saveVideo(userId, request))
                .build();
    }

    @GetMapping("/save/video")
    public AppResponse<List<String>> findSavedVideo(@RequestHeader("X-UserId") String userId,
                                                    @RequestParam("page") Integer page,
                                                    @RequestParam("pageSize") Integer pageSize) {
        return AppResponse.<List<String>>builder()
                .result(profileService.getSavedVideoIds(page, pageSize, userId)
                        .stream().toList())
                .build();
    }

    @PostMapping("/internal/compensation")
    public String compensation(@RequestBody CompensationRequest request) {
        compensationService.doCompensation(request.getSagaId());
        return "OK";
    }
}
