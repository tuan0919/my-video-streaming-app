package com.nlu.app.commandSide.service;

import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nlu.app.commandSide.commands.CreateRoleCommand;
import com.nlu.app.querySide.dto.request.RoleRequest;

@Service
public class RoleCommandService {
    private CommandGateway commandGateway;

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public CompletableFuture<String> createRole(RoleRequest request) {
        var command = CreateRoleCommand.builder()
                .description(request.getDescription())
                .name(request.getName())
                .permissions(request.getPermissions())
                .build();
        return commandGateway.send(command).thenApply(x -> "OK");
    }
}
