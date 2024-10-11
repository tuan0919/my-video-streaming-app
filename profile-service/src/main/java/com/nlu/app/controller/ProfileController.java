package com.nlu.app.controller;
import com.nlu.app.common.share.dto.CompensationRequest;
import com.nlu.app.common.share.dto.profile_service.request.FollowRequest;
import com.nlu.app.common.share.dto.profile_service.response.FollowerUserIdsResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.ICompensationService;
import com.nlu.app.service.IProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileController {
    IProfileService profileService;
    ICompensationService compensationService;

    @PostMapping("/internal/saga")
    public AppResponse<String> requestSaga(@RequestBody SagaAdvancedRequest sagaRequest) {
        return AppResponse.<String>builder()
                .result(profileService.sagaRequest(sagaRequest))
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

    @PostMapping("/internal/compensation")
    public String compensation(@RequestBody CompensationRequest request) {
        compensationService.doCompensation(request.getSagaId());
        return "OK";
    }
}
