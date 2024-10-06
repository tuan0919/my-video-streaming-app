package com.nlu.app.controller;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_UserDetailsDTO;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.QueryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QueryController {
    QueryService queryService;

    @GetMapping("/{userId}")
    public Mono<AppResponse<ClientView_UserDetailsDTO>> query(@PathVariable String userId) {
        return queryService.queryUserDetails(userId)
                .map(response -> AppResponse.<ClientView_UserDetailsDTO>builder()
                        .result(response)
                        .build());
    }
}
