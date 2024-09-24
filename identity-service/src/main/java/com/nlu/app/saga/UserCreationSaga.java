package com.nlu.app.saga;

import org.springframework.stereotype.Component;

import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserCreationSaga {
    private final UserService userService;
    private final OutboxRepository outboxRepository;
}
