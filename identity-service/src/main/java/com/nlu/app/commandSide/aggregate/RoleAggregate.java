package com.nlu.app.commandSide.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.HashSet;
import java.util.Set;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nlu.app.commandSide.commands.CreateRoleCommand;
import com.nlu.app.share.events.RoleCreatedEvent;
import com.nlu.app.share.query.FindAllPermissionByNamesQuery;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Aggregate
@RequiredArgsConstructor
@AllArgsConstructor
@Component
public class RoleAggregate {
    @AggregateIdentifier
    private String name;

    private String description;

    private QueryGateway queryGateway;

    @Autowired
    public void setQueryGateway(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @CommandHandler
    public RoleAggregate(CreateRoleCommand createRoleCommand) {
        this.name = createRoleCommand.getName();
        this.description = createRoleCommand.getDescription();
        var query = new FindAllPermissionByNamesQuery(createRoleCommand.getPermissions());
        Set<String> permissions = new HashSet<>(queryGateway
                .query(query, ResponseTypes.multipleInstancesOf(String.class))
                .join());
        var event = RoleCreatedEvent.builder()
                .name(createRoleCommand.getName())
                .description(createRoleCommand.getDescription())
                .permissions(permissions)
                .build();
        apply(event);
    }

    @EventSourcingHandler
    public void on(RoleCreatedEvent event) {
        this.name = event.getName();
        this.description = event.getDescription();
    }
}
