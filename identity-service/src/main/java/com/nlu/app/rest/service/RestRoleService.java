package com.nlu.app.rest.service;

import java.util.HashSet;
import java.util.Set;

import com.nlu.app.querySide.exception.ApplicationException;
import com.nlu.app.querySide.exception.ErrorCode;
import com.nlu.app.share.query.PermissionsExistsQuery;
import com.nlu.app.share.query.RoleExistsQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nlu.app.commandSide.commands.CreateRoleCommand;
import com.nlu.app.querySide.dto.request.RoleRequest;

@Service
public class CommandRoleService {
    private CommandGateway commandGateway;
    private QueryGateway queryGateway;

    @Autowired
    public void setQueryGateway(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public String createRole(RoleRequest request) {
        var command = CreateRoleCommand.builder()
                .description(request.getDescription())
                .name(request.getName())
                .permissions(request.getPermissions())
                .build();
        var roleQuery = RoleExistsQuery.builder()
                        .name(request.getName())
                        .build();
        if (queryGateway.query(roleQuery, Boolean.class).join()) {
            throw new ApplicationException(ErrorCode.ROLE_ALREADY_EXISTED);
        }
        var permissionQuery = new PermissionsExistsQuery(request.getPermissions());
        if (!queryGateway.query(permissionQuery, Boolean.class).join()) {
            throw new ApplicationException(ErrorCode.PERMISSION_NOT_EXISTED);
        }
        commandGateway.sendAndWait(command);
        return "OK";
    }
}
