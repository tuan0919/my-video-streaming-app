package com.nlu.app.commandSide.service;

import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nlu.app.commandSide.commands.CreateUserCommand;
import com.nlu.app.querySide.dto.request.UserCreationRequest;

@Service
public class CommandUserService {
    private CommandGateway commandGateway;

    @Autowired
    private void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public CompletableFuture<String> createUser(UserCreationRequest request) {
        var command = CreateUserCommand.builder()
                .dob(request.getDob())
                .email(request.getEmail())
                .city(request.getCity())
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        return commandGateway.send(command)
                .thenApply(x -> "OK");
    }
}
