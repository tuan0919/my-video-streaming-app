package com.nlu.app.commandSide.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nlu.app.commandSide.commands.CreateRoleCommand;
import com.nlu.app.domain.events.RoleCreatedEvent;

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
        var event = RoleCreatedEvent.builder()
                .name(createRoleCommand.getName())
                .description(createRoleCommand.getDescription())
                .permissions(createRoleCommand.getPermissions())
                .build();
        apply(event);
    }

    @EventSourcingHandler
    public void on(RoleCreatedEvent event) {
        this.name = event.getName();
        this.description = event.getDescription();
    }
}
