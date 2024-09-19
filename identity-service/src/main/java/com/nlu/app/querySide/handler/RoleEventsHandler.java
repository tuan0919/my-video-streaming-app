package com.nlu.app.querySide.handler;

import java.util.HashSet;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nlu.app.querySide.entity.Role;
import com.nlu.app.querySide.exception.ApplicationException;
import com.nlu.app.querySide.exception.ErrorCode;
import com.nlu.app.querySide.repository.PermissionRepository;
import com.nlu.app.querySide.repository.RoleRepository;
import com.nlu.app.share.events.RoleCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
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
