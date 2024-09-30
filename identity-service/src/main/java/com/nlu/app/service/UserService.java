package com.nlu.app.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.UserCreatedEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.dto.UserCreationDTO;
import com.nlu.app.common.event.UserCreationEvent;
import com.nlu.app.constant.PredefinedRole;
import com.nlu.app.dto.request.UserCreationRequest;
import com.nlu.app.dto.request.UserUpdateRequest;
import com.nlu.app.dto.response.UserResponse;
import com.nlu.app.entity.Outbox;
import com.nlu.app.entity.Role;
import com.nlu.app.entity.User;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import com.nlu.app.mapper.UserMapper;
import com.nlu.app.repository.OutboxRepository;
import com.nlu.app.repository.RoleRepository;
import com.nlu.app.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    OutboxRepository outboxRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();

        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        user.setEmailVerified(false);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ApplicationException(ErrorCode.USER_ALREADY_EXISTED);
        }
        UserCreationDTO creationDTO = UserCreationDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .timestamp(System.currentTimeMillis())
                .build();
        UserCreationEvent event = new UserCreationEvent(creationDTO);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Outbox outbox = Outbox.builder()
                    .aggregateType("created")
                    .sagaId(UUID.randomUUID().toString())
                    .aggregateId(user.getId())
                    .payload(objectMapper.writeValueAsString(event))
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public String test(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user.setEmailVerified(false);
        userRepository.save(user);
        ObjectMapper objectMapper = new ObjectMapper();
        var event = UserCreatedEvent.builder()
                .verified(false)
                .email(user.getEmail())
                .password(user.getPassword())
                .username(user.getUsername())
                .userId(user.getId())
                .build();
        try {
            Outbox outbox = Outbox.builder()
                    .aggregateType("identity.created")
                    .sagaId(UUID.randomUUID().toString())
                    .sagaAction(SagaAction.CREATE_NEW_USER)
                    .sagaStep(SagaAdvancedStep.IDENTITY_CREATE)
                    .sagaStepStatus(SagaStatus.SUCCESS)
                    .aggregateId(user.getId())
                    .payload(objectMapper.writeValueAsString(event))
                    .build();
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "OK";
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository
                .findByUsername(name)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED)));
    }
}
