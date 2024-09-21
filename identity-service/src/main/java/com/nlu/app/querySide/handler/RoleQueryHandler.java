package com.nlu.app.querySide.handler;

import com.nlu.app.querySide.repository.RoleRepository;
import com.nlu.app.querySide.repository.UserRepository;
import com.nlu.app.share.query.EmailExistsQuery;
import com.nlu.app.share.query.RoleExistsQuery;
import com.nlu.app.share.query.UsernameExistsQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleQueryHandler {
    RoleRepository roleRepository;

    @QueryHandler
    public boolean handle (RoleExistsQuery query) {
        return roleRepository.existsById(query.getName());
    }
}
