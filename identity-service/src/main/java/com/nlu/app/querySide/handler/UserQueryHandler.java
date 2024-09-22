package com.nlu.app.querySide.handler;

import java.util.Optional;

import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

import com.nlu.app.commandSide.state.mapper.UserMapper;
import com.nlu.app.commandSide.state.repository.UserRepository;
import com.nlu.app.domain.query.EmailExistsQuery;
import com.nlu.app.domain.query.GetUserByUsernameQuery;
import com.nlu.app.domain.query.GetUserQuery;
import com.nlu.app.domain.query.UsernameExistsQuery;
import com.nlu.app.rest.dto.response.UserResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserQueryHandler {
    UserRepository userRepository;
    UserMapper userMapper;
    QueryUpdateEmitter queryUpdateEmitter;

    @QueryHandler
    public boolean handle(UsernameExistsQuery query) {
        return userRepository.existsByUsername(query.getUsername());
    }

    @QueryHandler
    public boolean handle(EmailExistsQuery query) {
        return userRepository.existsByEmail(query.getEmail());
    }

    @QueryHandler
    public UserResponse handle(GetUserQuery query) {
        var oUser = userRepository.findById(query.getUserId());
        if (oUser.isPresent()) {
            var user = oUser.get();
            return userMapper.toUserResponse(user);
        }
        return null;
    }

    @QueryHandler
    public Optional<UserResponse> handle(GetUserByUsernameQuery query) {
        var oUser = userRepository.findByUsername(query.getUsername());
        if (!oUser.isPresent()) {
            return Optional.empty();
        }
        var user = oUser.get();
        return Optional.of(userMapper.toUserResponse(user));
    }
}
