package com.nlu.app.querySide.handler;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.nlu.app.commandSide.state.repository.RoleRepository;
import com.nlu.app.domain.query.RoleExistsQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleQueryHandler {
    RoleRepository roleRepository;

    @QueryHandler
    public boolean handle(RoleExistsQuery query) {
        return roleRepository.existsById(query.getName());
    }
}
