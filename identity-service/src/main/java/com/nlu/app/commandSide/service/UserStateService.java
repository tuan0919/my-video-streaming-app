package com.nlu.app.commandSide.service;

import org.springframework.stereotype.Service;

import com.nlu.app.commandSide.state.entity.User;
import com.nlu.app.commandSide.state.repository.UserRepository;
import com.nlu.app.rest.exception.ApplicationException;
import com.nlu.app.rest.exception.ErrorCode;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserStateService {
    private final UserRepository userRepository;

    public User getUserByUsername(@NonNull String username) {
        var oUser = userRepository.findByUsername(username);
        if (oUser.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        return oUser.get();
    }
}
