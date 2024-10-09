package com.nlu.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.dto.request.LikeVideoRequest;
import com.nlu.app.dto.request.SaveFileRequest;
import com.nlu.app.dto.request.SaveProcessVideoRequest;
import com.nlu.app.dto.request.VideoCreationRequest;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.dto.webclient.identity.request.TokenUserRequest;
import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Video;
import com.nlu.app.entity.VideoInteract;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.OutboxMapper;
import com.nlu.app.mapper.VideoInteractMapper;
import com.nlu.app.repository.IdentityWebClient;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.VideoInteractRepository;
import com.nlu.app.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InteractVideoService {
    VideoRepository videoRepository;
    VideoInteractRepository videoInteractRepository;
    OutboxRepository outboxRepository;
    VideoInteractMapper videoInteractMapper;
    OutboxMapper outboxMapper;

    @Transactional
    public Mono<String> upVoteVideo(LikeVideoRequest request) {
        var interact = getUserInteract(request.getVideoId(), request.getUserId());
        interact.setVote("UP_VOTE");
        videoInteractRepository.save(interact);
        var event = videoInteractMapper.toUpVotedEvent(interact);
        var outbox = outboxMapper.toSuccessOutbox(event, interact.getId(), SagaAction.VIDEO_UPVOTE);
        outboxRepository.save(outbox);
        return Mono.just("OK");
    }

    /**
     * Cập nhật tiến độ xem video của người dùng. hàm này sẽ lấy ra tương tác
     * của người dùng đối với video rồi sau đó cập nhật thông tin tiến độ vào đó.
     * Nếu tương tác không tồn tại thì sẽ tạo mới.
     * @param request: dto của request.
     * @exception ApplicationException
     *  (RESOURCE_NOT_FOUND): Nếu video không tồn tại
     */
    @Transactional
    public Mono<String> saveProgress(SaveProcessVideoRequest request) {
        var interact = getUserInteract(request.getVideoId(), request.getUserId());
        interact.setProgress(request.getProgress());
        videoInteractRepository.save(interact);
        return Mono.just("OK");
    }

    /**
     * Lấy ra tương tác của video với video hiện tại, nếu tương tác không tồn tại thì tạo mới.
     * Hành động tạo mới tương tác sẽ đồng thời xem như là user đã xem video này.
     * @param videoId
     * @param userId
     * @return Tương tác của user đối với video hiện tại.
     * @exception ApplicationException
     *  (RESOURCE_NOT_FOUND): Nếu video không tồn tại
     */
    private VideoInteract getUserInteract(String videoId, String userId) {
        var oVideo = videoRepository.findById(videoId);
        if (oVideo.isEmpty()) {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        var video = oVideo.get();
        var oInteract = videoInteractRepository.findByVideoVideoIdAndUserId(videoId, userId);
        VideoInteract interact = null;
        if (oInteract.isEmpty()) {
            interact = VideoInteract.builder()
                    .userId(userId)
                    .video(video)
                    .build();
            firstTimeInteract(interact, video);
        } else {
            interact = oInteract.get();
        }
        return interact;
    }

    /**
     *  Tạo ra tương tác đầu tiên của user đối với video hiện tại,
     *     đồng thời bắn ra event ViewedVideoEvent nghĩa là đánh dấu user đã xem video này.
     * @param interact: tương tác lần đầu tiên của user đối với video
     * @param video: video mà user tương tác đến
     */
    private void firstTimeInteract(VideoInteract interact, Video video) {
        video.getInteractions().add(interact);
        videoInteractRepository.save(interact);
        videoRepository.save(video);
        var event = videoInteractMapper.toViewedVideoEvent(interact);
        var outbox = outboxMapper.toSuccessOutbox(event, video.getVideoId(), SagaAction.MARK_VIEW_VIDEO);
        outboxRepository.save(outbox);
    }
}
