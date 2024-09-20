package com.nlu.app.querySide.handler;

import java.util.Set;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nlu.app.commandSide.commands.AddRoleCommand;
import com.nlu.app.querySide.entity.User;
import com.nlu.app.querySide.exception.ApplicationException;
import com.nlu.app.querySide.exception.ErrorCode;
import com.nlu.app.querySide.repository.RoleRepository;
import com.nlu.app.querySide.repository.UserRepository;
import com.nlu.app.share.events.UserCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventsHandler {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @EventHandler
    @Transactional
    public void on(UserCreatedEvent event) {
        User user = User.builder()
                .id(event.getUserId())
                .roles(Set.of())
                .emailVerified(false)
                .username(event.getUsername())
                .password(passwordEncoder.encode(event.getPassword()))
                .email(event.getEmail())
                .build();
        userRepository.save(user);
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
