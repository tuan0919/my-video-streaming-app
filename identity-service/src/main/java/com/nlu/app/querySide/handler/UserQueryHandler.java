package com.nlu.app.querySide.handler;

import com.nlu.app.commandSide.commands.AddRoleCommand;
import com.nlu.app.querySide.entity.User;
import com.nlu.app.querySide.exception.ApplicationException;
import com.nlu.app.querySide.exception.ErrorCode;
import com.nlu.app.querySide.repository.RoleRepository;
import com.nlu.app.querySide.repository.UserRepository;
import com.nlu.app.share.events.UserCreatedEvent;
import com.nlu.app.share.query.EmailExistsQuery;
import com.nlu.app.share.query.UsernameExistsQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserQueryHandler {
    UserRepository userRepository;

    @QueryHandler
    public boolean handle (UsernameExistsQuery query) {
        return userRepository.existsByUsername(query.getUsername());
    }

    @QueryHandler
    public boolean handle (EmailExistsQuery query) {
        return userRepository.existsByEmail(query.getEmail());
    }
}
