package com.nlu.app.rest.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.nlu.app.application.identity.query.EmailExistsQuery;
import com.nlu.app.application.identity.query.GetUserByUsernameQuery;
import com.nlu.app.application.identity.query.RoleExistsQuery;
import com.nlu.app.application.identity.query.UsernameExistsQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.nlu.app.application.identity.command.CreateUserCommand;
import com.nlu.app.rest.constant.PredefinedRole;
import com.nlu.app.rest.dto.request.UserCreationRequest;
import com.nlu.app.rest.dto.response.UserResponse;
import com.nlu.app.rest.exception.ApplicationException;
import com.nlu.app.rest.exception.ErrorCode;

@Service
public class RestUserService {
    private CommandGateway commandGateway;
    private QueryGateway queryGateway;
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    public void setQueryGateway(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @Autowired
    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public String createUser(UserCreationRequest request, String sessionId) {
        var command = CreateUserCommand.builder()
                .dob(request.getDob())
                .email(request.getEmail())
                .city(request.getCity())
                .password(request.getPassword())
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(new HashSet<>(List.of(PredefinedRole.USER_ROLE)))
                .requestId(sessionId)
                .build();
        String username = request.getUsername();
        String email = request.getEmail();
        var queryUsername = UsernameExistsQuery.builder().username(username).build();
        var queryEmail = EmailExistsQuery.builder().email(email).build();
        var queryRole = RoleExistsQuery.builder().name(PredefinedRole.USER_ROLE).build();
        if (queryGateway.query(queryUsername, Boolean.class).join()) {
            throw new ApplicationException(ErrorCode.USER_ALREADY_EXISTED);
        }
        if (queryGateway.query(queryEmail, Boolean.class).join()) {
            throw new ApplicationException(ErrorCode.USER_ALREADY_EXISTED);
        }
        if (!queryGateway.query(queryRole, Boolean.class).join()) {
            throw new ApplicationException(ErrorCode.ROLE_NOT_EXISTED);
        }
        commandGateway.sendAndWait(command);
        return sessionId;
    }
}
