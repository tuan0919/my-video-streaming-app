package com.nlu.app.service;

import com.nlu.app.common.share.dto.profile_service.request.FollowRequest;
import com.nlu.app.common.share.dto.profile_service.request.ProfileCreationRequest;
import com.nlu.app.common.share.dto.profile_service.response.FollowerUserIdsResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileCreationResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.exception.ApplicationException;

public interface IProfileService {
    String follow(FollowRequest request) throws ApplicationException;
    FollowerUserIdsResponse getFollowerIds(String userId) throws ApplicationException;
    ProfileCreationResponse insert (ProfileCreationRequest request) throws ApplicationException;
    ProfileResponseDTO getUserProfile(String userId) throws ApplicationException;
    String sagaRequest (SagaAdvancedRequest sagaRequest) throws ApplicationException;
}
