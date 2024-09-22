package com.nlu.app.querySide.handler;

import java.util.HashSet;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nlu.app.commandSide.commands.AddRoleCommand;
import com.nlu.app.commandSide.state.entity.User;
import com.nlu.app.commandSide.state.mapper.UserMapper;
import com.nlu.app.commandSide.state.repository.RoleRepository;
import com.nlu.app.commandSide.state.repository.UserRepository;
import com.nlu.app.domain.events.UserCreatedEvent;
import com.nlu.app.domain.query.GetUserByUsernameQuery;
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
    }

    @EventHandler
    @Transactional
    public void on(AddRoleCommand command) {
        var oUser = userRepository.findById(command.getUserId());
        if (oUser.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        var oRole = roleRepository.findById(command.getRoleName());
        if (oRole.isEmpty()) {
            throw new ApplicationException(ErrorCode.ROLE_NOT_EXISTED);
        }
        var user = oUser.get();
        var role = oRole.get();
        user.getRoles().add(role);
        userRepository.save(user);
    }
}
