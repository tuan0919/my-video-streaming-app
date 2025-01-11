package com.nlu.app.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.dto.file_service.request.MoveFileRequest;
import com.nlu.app.common.share.dto.file_service.request.UploadFileRequest;
import com.nlu.app.common.share.dto.profile_service.request.*;
import com.nlu.app.common.share.dto.profile_service.response.*;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Profile;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.mapper.ProfileMapper;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService implements IProfileService {
    private final ProfileRepository profileRepository;
    private final OutboxRepository outboxRepository;
    private final OutboxMapper outboxMapper;
    private final ProfileMapper profileMapper;
    private final ObjectMapper objectMapper;
    private final FileService fileService;

    @Transactional
    public String follow(FollowRequest request, String userId) {
        var oProfile = profileRepository.findProfileByUserId(request.getFollowId());
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        var followProfile = oProfile.get();
        var userProfile = profileRepository.findProfileByUserId(userId).get();
        userProfile.getFollow().add(followProfile);
        followProfile.getFollowers().add(userProfile);
        profileRepository.save(userProfile);
        profileRepository.save(followProfile);
        return "OK";
    }

    @Override
    public GetLinkUploadAvatarResponse getLinkForUpload(GetLinkUploadAvatarRequest request, String userId, String username) {
        var oProfile = profileRepository.findProfileByUserId(userId);
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        String fileName = request.getFileName();
        String fileKey = "temp/"+username+"/"+fileName;
        UploadFileRequest uploadRequest = new UploadFileRequest(fileKey);
        String link = fileService.getUrlUploadToTemp(uploadRequest).getLink();
        return new GetLinkUploadAvatarResponse(link);
    }

    @Override
    @Transactional
    public ChangeAvatarResponse changeAvatar(ChangeAvatarRequest request, String username, String userId) {
        var oProfile = profileRepository.findProfileByUserId(userId);
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        String oldKey = "temp/"+username+"/"+request.getAvatarKey();
        String extension = oldKey.substring(oldKey.lastIndexOf(".") + 1);
        String newKey = UUID.randomUUID().toString() + "." + extension;
        newKey = "inventory/"+username+"/"+newKey;
        var requestMoveFile = new MoveFileRequest(oldKey, newKey);
        fileService.moveFile(requestMoveFile);
        var profile = oProfile.get();
        profile.setAvatarId(newKey);
        profileRepository.save(profile);
        var link = fileService.generateResourceURL(newKey);
        return new ChangeAvatarResponse(link);
    }

    public FollowerUserIdsResponse getFollowerIds(String userId) {
        var oProfile = profileRepository.findProfileByUserId(userId);
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        var profile = oProfile.get();
        // TODO: map set follower's profiles to list userId
        var ids = profile.getFollowers()
                .stream().map(Profile::getUserId).toList();
        return FollowerUserIdsResponse.builder()
                .followers(ids)
                .build();
    }

    @Transactional
    public ProfileCreationResponse insert(ProfileCreationRequest request) {
        String sagaId = request.getSagaId();
        var profile = profileMapper.toEntity(request);
        profileRepository.save(profile);
        var event = profileMapper.toProfileCreatedEvent(profile);
        Outbox outbox = outboxMapper.toSuccessOutbox(event, sagaId, SagaAction.CREATE_NEW_USER);
        outboxRepository.save(outbox);
        return profileMapper.toResponseCreationDTO(profile);
    }

    @Transactional
    public void update(String userId, UpdateProfileRequest request) {
        var oProfile = profileRepository.findProfileByUserId(userId);
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.PROFILE_NOT_EXISTED);
        }
        var profile = oProfile.get();
        profile.setAddress(request.getAddress());
        profile.setCountry(request.getCountry());
        profile.setFullName(request.getFullName());
        profile.setGender(request.getGender());
        var event = profileMapper.toProfileUpdatedEvent(profile);
        Outbox outbox = outboxMapper.toSuccessOutbox(event, profile.getUserId(), SagaAction.UPDATE_PROFILE);
        outboxRepository.save(outbox);
    }

    /**
     * Kiểm tra xem liệu một user có id là userId có đang follow một user khác có id là followId không
     * @param userId
     * @param followId
     * @return true nếu đúng.
     */
    
    public Boolean checkUserFollow(String userId, String followId, String username) {
        var oTargetProfile = profileRepository.findProfileByUserId(followId);
        if (oTargetProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.PROFILE_NOT_EXISTED);
        }
        var oUserProfile = profileRepository.findProfileByUserId(userId);
        if (oUserProfile.isEmpty()) {
            log.warn("What the heck? Tại sao user {} không có profile?", username);
            throw new ApplicationException(ErrorCode.PROFILE_NOT_EXISTED);
        }
        return oTargetProfile.get().getFollowers().contains(oUserProfile.get());
    }

    @Override
    @Transactional
    public ProfileResponseDTO getUserProfile(String userId) {
        var oProfile = profileRepository.findProfileByUserId(userId);
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        var dto = profileMapper.toResponseDTO(oProfile.get());
        String avatar;
        try {
             avatar = fileService.generateResourceURL(oProfile.get().getAvatarId());
        } catch (Exception e) {
            avatar = "https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg";
        }
        dto.setAvatar(avatar);
        return dto;
    }

    public Map<String, ProfileResponseDTO> getUserProfile(List<String> userIds) {
        var list = profileRepository.findProfilesByUserIdIn(userIds);
        var mapURLs = fileService.generateResourceURLs(list.stream()
                .map(Profile::getAvatarId)
                .toList());
        var map = new HashMap<String, ProfileResponseDTO>();
        for (var profile : list) {
            String avatarLink =
                    Optional.ofNullable(mapURLs.get(profile.getAvatarId()))
                    .orElse("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSwdIVSqaMsmZyDbr9mDPk06Nss404fosHjLg&s");
            var dto = profileMapper.toResponseDTO(profile);
            dto.setAvatar(avatarLink);
            map.put(profile.getUserId(), dto);
        }
        return map;
    }

    public ProfileFollowStatusResponse getFollowStatus(String userId) {
        var oProfile = profileRepository.findProfileByUserId(userId);
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        var profile = oProfile.get();
        return profileMapper.toResponseFollowStatusDTO(profile);
    }

    @Transactional
    public String saveVideo(String userId, SaveVideoRequest request) {
        var oProfile = profileRepository.findProfileByUserId(userId);
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.PROFILE_NOT_EXISTED);
        }
        var profile = oProfile.get();
        profile.getSavedVideoIds().add(request.getVideoId());
        profileRepository.save(profile);
        return "OK";
    }

    public Page<String> getSavedVideoIds(Integer page, Integer pageSize, String userId) {
        var oProfile = profileRepository.findProfileByUserId(userId);
        if (oProfile.isEmpty()) {
            throw new ApplicationException(ErrorCode.PROFILE_NOT_EXISTED);
        }
        var profile = oProfile.get();
        Pageable pageable = PageRequest.of(page, pageSize);
        return profileRepository.findSavedVideosByProfileId(profile.getProfileId(), pageable);
    }

    @Override
    public String sagaRequest(SagaAdvancedRequest sagaRequest) {
        String sagaStep = sagaRequest.getSagaStep();
        try {
            switch (sagaStep) {
                case SagaAdvancedStep.PROFILE_CREATE ->  {
                    return sagaProfileCreate(sagaRequest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION);
        }
        throw new ApplicationException(ErrorCode.UNKNOWN_ACTION);
    }

    @Transactional
    String sagaProfileCreate(SagaAdvancedRequest sagaRequest) throws JsonProcessingException {
        var request = objectMapper.readValue(sagaRequest.getPayload(), ProfileCreationRequest.class);
        String sagaId = sagaRequest.getSagaId();
        String sagaAction = sagaRequest.getSagaAction();
        var profile = profileMapper.toEntity(request);
        profileRepository.save(profile);
        var event = profileMapper.toProfileCreatedEvent(profile);
        Outbox outbox = outboxMapper.toSuccessOutbox(event, sagaId, sagaAction);
        outboxRepository.save(outbox);
        return "OK";
    }

    public String create(ProfileCreationRequest request) {
        var profile = profileMapper.toEntity(request);
        profileRepository.save(profile);
        var event = profileMapper.toProfileCreatedEvent(profile);
        Outbox outbox = outboxMapper.toSuccessOutbox(event, request.getUserId(), SagaAction.CREATE_NEW_USER);
        outboxRepository.save(outbox);
        return "OK";
    }
}
