package com.nlu.app.controller;

import com.nlu.app.dto.AppResponse;
import com.nlu.app.service.SagaTaskService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sagaTask")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SagaController {
    SagaTaskService sagaTaskService;
    @GetMapping("/{sagaId}")
    AppResponse<?> getResult(@PathVariable String sagaId) {
        return AppResponse.builder()
                .result(sagaTaskService.getResult(sagaId))
                .build();
    }
}
