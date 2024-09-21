package com.nlu.app.querySide.handler;

import com.nlu.app.querySide.repository.PermissionRepository;
import com.nlu.app.share.query.PermissionsExistsQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionQueryHandler {
    PermissionRepository permissionRepository;

    @QueryHandler
    public boolean handle (PermissionsExistsQuery query) {
        return permissionRepository.allExistsById(query.getNames(), (long) query.getNames().size());
    }
}
