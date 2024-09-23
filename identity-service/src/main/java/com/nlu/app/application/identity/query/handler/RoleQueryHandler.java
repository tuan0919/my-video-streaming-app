package com.nlu.app.application.identity.query.handler;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.nlu.app.domain.identity.RoleRepository;
import com.nlu.app.application.identity.query.RoleExistsQuery;

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
