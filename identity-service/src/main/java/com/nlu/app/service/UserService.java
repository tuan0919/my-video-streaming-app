package com.nlu.app.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.IdentityUpdatedEvent;
import com.nlu.app.common.share.event.UserCreatedEvent;
import com.nlu.app.mapper.OutboxMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    OutboxMapper outboxMapper;
    PasswordEncoder passwordEncoder;
    RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public String createUser(UserCreationRequest request) {
        String sagaId = UUID.randomUUID().toString();
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ApplicationException(ErrorCode.USER_ALREADY_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();

        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user.setEmailVerified(false);

        userRepository.save(user);
        var event = userMapper.toUserCreatedEvent(user);

        Outbox outbox = outboxMapper.toSuccessOutbox(event, sagaId);
        outboxRepository.save(outbox);
        return sagaId;
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository
                .findByUsername(name)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAnyRole('ADMIN')") @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        String sagaId = UUID.randomUUID().toString();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));

        // cache thông tin event bù trừ nếu saga này thất bại
        var compensationEvent = userMapper.toIdentityUpdatedEvent(user);
        redisTemplate.opsForValue().set(sagaId, compensationEvent);

        String hashPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashPassword);
        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);

        var updatedEvent = userMapper.toIdentityUpdatedEvent(user);
        Outbox outbox = outboxMapper.toSuccessOutbox(updatedEvent, sagaId);
        outboxRepository.save(outbox);
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @Transactional
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED)));
    }
}
