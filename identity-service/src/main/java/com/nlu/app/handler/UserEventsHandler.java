package com.nlu.app.handler;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nlu.app.entity.User;
import com.nlu.app.events.UserCreateEvent;
import com.nlu.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserEventsHandler {
    private final UserRepository userRepository;

    @Transactional
    public Optional<User> onUserCreateEvent(UserCreateEvent event) {
        boolean isExisted = userRepository.findById(event.getUserId()).isPresent();
        if (!isExisted) {
            User user = new User();
            user.setId(event.getUserId());
            user.setUsername(event.getUsername());
            user.setPassword(event.getPassword());
            user.setEmail(event.getEmail());
            user.setRoles(Set.of(event.getRole()));
            userRepository.save(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
