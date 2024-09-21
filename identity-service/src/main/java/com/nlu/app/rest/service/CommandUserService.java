package com.nlu.app.rest.service;
import com.nlu.app.querySide.constant.PredefinedRole;
import com.nlu.app.querySide.exception.ApplicationException;
import com.nlu.app.querySide.exception.ErrorCode;
import com.nlu.app.share.query.EmailExistsQuery;
import com.nlu.app.share.query.RoleExistsQuery;
import com.nlu.app.share.query.UsernameExistsQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nlu.app.commandSide.commands.CreateUserCommand;
import com.nlu.app.querySide.dto.request.UserCreationRequest;

@Service
public class CommandUserService {
    private CommandGateway commandGateway;
    private QueryGateway queryGateway;

    @Autowired
    private void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    public void setQueryGateway(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    public String createUser(UserCreationRequest request) {
        var command = CreateUserCommand.builder()
                .dob(request.getDob())
                .email(request.getEmail())
                .city(request.getCity())
                .password(request.getPassword())
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
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
        return "OK";
    }
}
