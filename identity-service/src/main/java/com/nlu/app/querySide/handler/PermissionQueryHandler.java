package com.nlu.app.querySide.handler;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.nlu.app.commandSide.state.repository.PermissionRepository;
import com.nlu.app.domain.query.PermissionsExistsQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionQueryHandler {
    PermissionRepository permissionRepository;

    @QueryHandler
    public boolean handle(PermissionsExistsQuery query) {
        return permissionRepository.allExistsById(
                query.getNames(), (long) query.getNames().size());
    }
}
