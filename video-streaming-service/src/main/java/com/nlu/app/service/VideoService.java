package com.nlu.app.service;

import com.nlu.app.common.share.SagaAction;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.request.VideoCreationRequest;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.entity.Video;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.mapper.ResponseDTOMapper;
import com.nlu.app.mapper.VideoMapper;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.VideoInteractRepository;
import com.nlu.app.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VideoService {
    VideoRepository videoRepository;
    FileService fileService;
    ResponseDTOMapper responseMapper;
    OutboxRepository outboxRepository;
    OutboxMapper outboxMapper;
    VideoMapper videoMapper;
    VideoInteractRepository interactRepository;

    public VideoCreationResponse createVideo(String userId, String username, VideoCreationRequest request) {
        var requestSaveFile = SaveFileRequest.builder().filename(request.getVideoKey()).build();
        var response = fileService.moveToInventory(requestSaveFile, userId, username);
        var result = _insertVideo_(request, userId, response.getKey());
        return _mapToResponse_(result);
    }

    @Transactional
    Video _insertVideo_(VideoCreationRequest request, String userId, String key) {
        var video = videoMapper.toEntity(request, userId, key);
        videoRepository.save(video);
        var videoEvent = videoMapper.toNewVideoCreatedEvent(video);
        var outbox = outboxMapper.toSuccessOutbox(videoEvent, videoEvent.getVideoId(), SagaAction.CREATE_NEW_VIDEO);
        outboxRepository.save(outbox);
        return video;
    }

    private VideoCreationResponse _mapToResponse_(Video video) {
        var response = fileService.generateResourceURL(video.getVideoKey());
        String link = response;
        return videoMapper.toVideoCreationResponse(video, link);
    }

    public VideoDetailsResponse getVideoDetails(String videoId, String userId) {
        var oVideo = videoRepository.findById(videoId);
        if (oVideo.isEmpty()) {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        var video = oVideo.get();
        var oInteract = interactRepository.findByVideoVideoIdAndUserId(videoId, userId);
        var interact = oInteract.orElse(null);
        String videoLink = fileService.generateResourceURL(video.getVideoKey());
        var response = responseMapper.toResponseDTO(video, interact, videoLink);
        Integer downVote = interactRepository.countDistinctByVoteAndVideo_VideoId("DOWN_VOTE", videoId);
        Integer upVote = interactRepository.countDistinctByVoteAndVideo_VideoId("UP_VOTE", videoId);
        response.setUpVote(upVote);
        response.setDownVote(downVote);
        return response;
    }

    public Boolean checkExists(String videoId) {
        var oVideo = videoRepository.findById(videoId);
        return oVideo.isPresent();
    }
}
