package com.nlu.app.application.identity.query.event;

import org.springframework.stereotype.Component;

import com.nlu.app.application.identity.query.repository.PermissionRepository;
import com.nlu.app.application.identity.query.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleEventsHandler {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
}
