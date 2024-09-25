package com.nlu.app.application.identity.query.event;

import com.nlu.app.application.identity.query.entity.Role;
import com.nlu.app.domain.identity.event.RoleCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import com.nlu.app.application.identity.query.repository.PermissionRepository;
import com.nlu.app.application.identity.query.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleEventsHandler {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    @EventHandler
    @Transactional
    public void on (RoleCreatedEvent roleCreatedEvent) {
        Role role = Role.builder()
                .name(roleCreatedEvent.getName())
                .permissions(Set.of())
                .description(roleCreatedEvent.getDescription())
                .build();
        roleRepository.save(role);
    }
}
