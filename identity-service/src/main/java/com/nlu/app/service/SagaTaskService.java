package com.nlu.app.service;

import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SagaTaskService {
    RedisTemplate<String, Object> redisTemplate;

    public Object getResult(String sagaId) {
        if (redisTemplate.hasKey("SAGA_COMPLETED_" + sagaId)) {
            return redisTemplate.opsForValue().get("SAGA_COMPLETED_" + sagaId);
        } else if (redisTemplate.hasKey("SAGA_ABORTED_" + sagaId)) {
            return redisTemplate.opsForValue().get("SAGA_ABORTED_" + sagaId);
        } else {
            throw new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
