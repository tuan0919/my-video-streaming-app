package com.nlu.app.common.share.webclient;

import com.nlu.app.common.share.dto.AppResponse;
import com.nlu.app.common.share.dto.notification_service.response.NotificationResponse;
import com.nlu.app.common.share.dto.notification_service.response.UnreadCountResponse;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import java.util.List;

public interface NotificationWebClient {
    @GetExchange(url = "notification/users")
    Mono<AppResponse<List<NotificationResponse>>> getNotifications(@RequestHeader("X-UserId") String userId,
                                                                   @RequestParam("page") Integer page,
                                                                   @RequestParam("pageSize") Integer pageSize);

    @PostExchange(url = "notification/users/count")
    Mono<AppResponse<UnreadCountResponse>> countUnread(@RequestHeader("X-UserId") String userId);
}
