package com.nlu.app.service;

import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.dto.file_service.request.MoveFileRequest;
import com.nlu.app.common.share.dto.file_service.request.UploadFileRequest;
import com.nlu.app.common.share.dto.file_service.response.SignedURLResponse;
import com.nlu.app.dto.request.PutFileRequest;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.request.VideoCreationRequest;
import com.nlu.app.dto.response.SaveFileResponse;
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
import com.nlu.app.repository.VideoPagingRepository;
import com.nlu.app.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VideoService {
    VideoRepository videoRepository;
    VideoPagingRepository videoPagingRepository;
    ResponseDTOMapper responseMapper;
    OutboxRepository outboxRepository;
    OutboxMapper outboxMapper;
    VideoMapper videoMapper;
    VideoInteractRepository interactRepository;
    FileService fileService;

    public VideoCreationResponse createVideo(String userId, String username, VideoCreationRequest request) {
        var requestSaveFile_Video = SaveFileRequest.builder().filename(request.getVideoKey()).build();
        var requestSaveFile_Thumbnail = SaveFileRequest.builder().filename(request.getThumbnailKey()).build();
        var saveVideoResponse = moveToInventory(requestSaveFile_Video, userId, username);
        var saveThumbnailResponse = moveToInventory(requestSaveFile_Thumbnail, userId, username);
        var result = _insertVideo_(request, userId, saveVideoResponse.getKey(), saveThumbnailResponse.getKey());
        return _mapToResponse_(result);
    }

    public VideoCreationResponse createVideoWithoutThumbnail(String userId, String username, VideoCreationRequest request) {
        var requestSaveFile_Video = SaveFileRequest.builder().filename(request.getVideoKey()).build();
        var saveVideoResponse = moveToInventory(requestSaveFile_Video, userId, username);
        var result = _insertVideo_(request, userId, saveVideoResponse.getKey());
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

    @Transactional
    Video _insertVideo_(VideoCreationRequest request, String userId, String videoKey, String thumbnailKey) {
        var video = videoMapper.toEntity(request, userId, videoKey, thumbnailKey);
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
        String videoLink;
        try {
            videoLink = fileService.generateResourceURL(video.getVideoKey());
        } catch (Exception e) {
            videoLink = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
        }
        String thumbnail; // trong trường hợp thumbnail không tồn tại thì sử dụng thumbnail default
        try {
            thumbnail = fileService.generateResourceURL(video.getThumbnailKey());
        } catch (Exception e) {
            e.printStackTrace();
            thumbnail = "https://ff.hcmuaf.edu.vn/data/image/slide/nonglam1.jpg";
        }
        var response = responseMapper.toResponseDTO(video, interact, videoLink, thumbnail);
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

    public SaveFileResponse moveToInventory(SaveFileRequest request, String userId, String username) {
        String oldKey = "temp/"+username+"/"+request.getFilename();
        String extension = oldKey.substring(oldKey.lastIndexOf(".") + 1);
        String newKey = UUID.randomUUID() + "." + extension;
        newKey = "inventory/"+username+"/"+newKey;
        var requestMoveFile = new MoveFileRequest(oldKey, newKey);
        fileService.moveFile(requestMoveFile);
        return SaveFileResponse.builder()
                .key(newKey)
                .build();
    }

    public Page<String> videoIdsFromStart(Integer page, Integer pageSize) {
        var pageable = PageRequest.of(page, pageSize);
        return videoPagingRepository.fetchFromStart(pageable);
    }

    public Page<String> videoIdsFromStart(Integer page, Integer pageSize, String excludeId) {
        var pageable = PageRequest.of(page, pageSize);
        return videoPagingRepository.fetchFromStartExcludeId(pageable, excludeId);
    }

    public SignedURLResponse getUrlUploadToTemp (PutFileRequest request, String userId, String username) {
        String fileName = request.getFilename();
        String fileKey = "temp/"+username+"/"+fileName;
        UploadFileRequest uploadRequest = new UploadFileRequest(fileKey);
        return fileService.getUrlUploadToTemp(uploadRequest);
    }
}
