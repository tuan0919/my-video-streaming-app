package com.nlu.app.querySide.handler;

import java.util.HashSet;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nlu.app.commandSide.state.entity.Role;
import com.nlu.app.commandSide.state.repository.PermissionRepository;
import com.nlu.app.commandSide.state.repository.RoleRepository;
import com.nlu.app.domain.events.RoleCreatedEvent;
import com.nlu.app.rest.exception.ApplicationException;
import com.nlu.app.rest.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleEventsHandler {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    @EventHandler
    @Transactional
    public void on(RoleCreatedEvent roleCreatedEvent) {
        var oRole = roleRepository.findById(roleCreatedEvent.getName());
        if (oRole.isPresent()) throw new ApplicationException(ErrorCode.ROLE_ALREADY_EXISTED);
        var permissions = permissionRepository.findAllById(roleCreatedEvent.getPermissions());
        Role role = Role.builder()
                .name(roleCreatedEvent.getName())
                .description(roleCreatedEvent.getDescription())
                .permissions(new HashSet<>(permissions))
                .build();
        roleRepository.save(role);
    }
}
