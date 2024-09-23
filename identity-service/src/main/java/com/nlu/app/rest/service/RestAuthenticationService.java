package com.nlu.app.rest.service;

import com.nlu.app.application.identity.command.LoginCommand;
import com.nlu.app.application.identity.query.GetUserByUsernameQuery;
import com.nlu.app.rest.dto.request.AuthenticationRequest;
import com.nlu.app.rest.dto.response.AuthenticationResponse;
import com.nlu.app.rest.dto.response.UserResponse;
import com.nlu.app.rest.exception.ApplicationException;
import com.nlu.app.rest.exception.ErrorCode;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RestAuthenticationService {
    private CommandGateway commandGateway;
    private QueryGateway queryGateway;

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    public void setQueryGateway(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var queryUser = GetUserByUsernameQuery.builder().username(request.getUsername()).build();
        var subCheck = queryGateway.subscriptionQuery(
                queryUser,
                ResponseTypes.optionalInstanceOf(UserResponse.class),
                ResponseTypes.optionalInstanceOf(UserResponse.class)
        );
        var o = subCheck.initialResult().block(Duration.ofSeconds(3));
        if (o.isEmpty()) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        var id = o.get().getId();
        var commandLogin = LoginCommand.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .userId(id).build();
        commandGateway.sendAndWait(commandLogin);
        return null;
    }
}
