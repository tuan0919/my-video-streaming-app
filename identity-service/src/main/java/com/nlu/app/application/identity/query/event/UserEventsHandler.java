package com.nlu.app.application.identity.query.event;

import java.util.HashSet;

import com.nlu.app.domain.identity.event.RoleAddedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nlu.app.application.identity.query.entity.User;
import com.nlu.app.application.identity.query.entity.UserMapper;
import com.nlu.app.application.identity.query.repository.RoleRepository;
import com.nlu.app.application.identity.query.repository.UserRepository;
import com.nlu.app.domain.identity.event.UserCreatedEvent;
import com.nlu.app.application.identity.query.GetUserByUsernameQuery;
import com.nlu.app.rest.exception.ApplicationException;
import com.nlu.app.rest.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventsHandler {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    QueryUpdateEmitter queryUpdateEmitter;
    UserMapper userMapper;
    SimpMessagingTemplate messagingTemplate;

    @EventHandler
    @Transactional
    public void on(UserCreatedEvent event) {
        var roles = roleRepository.findAllById(event.getRoles());
        User user = User.builder()
                .id(event.getUserId())
                .roles(new HashSet<>(roles))
                .emailVerified(false)
                .username(event.getUsername())
                .password(passwordEncoder.encode(event.getPassword()))
                .email(event.getEmail())
                .build();
        userRepository.save(user);
        queryUpdateEmitter.emit(
                GetUserByUsernameQuery.class,
                query -> query.getUsername().equals(event.getUsername()),
                userMapper.toUserResponse(user));
        String requestId = event.getRequestId();
        messagingTemplate.convertAndSendToUser(requestId, "/topic/user-created", userMapper.toUserResponse(user));
    }

    @EventHandler
    @Transactional
    public void on(RoleAddedEvent event) {
        var oUser = userRepository.findById(event.getUserId());
        if (oUser.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        var oRole = roleRepository.findById(event.getRoleName());
        if (oRole.isEmpty()) {
            throw new ApplicationException(ErrorCode.ROLE_NOT_EXISTED);
        }
        var user = oUser.get();
        var role = oRole.get();
        user.getRoles().add(role);
        userRepository.save(user);
    }
}
