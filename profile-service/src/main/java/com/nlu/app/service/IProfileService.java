package com.nlu.app.service;

import com.nlu.app.common.share.dto.profile_service.request.ChangeAvatarRequest;
import com.nlu.app.common.share.dto.profile_service.request.FollowRequest;
import com.nlu.app.common.share.dto.profile_service.request.GetLinkUploadAvatarRequest;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.profile_service.response.*;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.exception.ApplicationException;

public interface IProfileService {
    String follow(FollowRequest request, String userId) throws ApplicationException;
    FollowerUserIdsResponse getFollowerIds(String userId) throws ApplicationException;
    ProfileCreationResponse insert (ProfileCreationRequest request) throws ApplicationException;
    ProfileResponseDTO getUserProfile(String userId) throws ApplicationException;
    String sagaRequest (SagaAdvancedRequest sagaRequest) throws ApplicationException;
    Boolean checkUserFollow(String userId, String followId, String username);
    GetLinkUploadAvatarResponse getLinkForUpload(GetLinkUploadAvatarRequest request, String username, String userId);
    ChangeAvatarResponse changeAvatar(ChangeAvatarRequest request, String username, String userId);
}
