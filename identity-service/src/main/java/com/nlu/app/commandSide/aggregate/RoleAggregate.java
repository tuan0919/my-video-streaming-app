package com.nlu.app.commandSide.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.stereotype.Component;

import com.nlu.app.commandSide.commands.CreateRoleCommand;
import com.nlu.app.commandSide.events.RoleCreatedEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Aggregate
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Component
public class RoleAggregate {
    @AggregateIdentifier
    private String name;

    private String description;

    @CommandHandler
    public RoleAggregate(CreateRoleCommand createRoleCommand) {
        this.name = createRoleCommand.getName();
        this.description = createRoleCommand.getDescription();
        var event = RoleCreatedEvent.builder()
                .name(createRoleCommand.getName())
                .description(createRoleCommand.getDescription())
                .build();
        apply(event);
    }

    @EventSourcingHandler
    public void on(RoleCreatedEvent event) {
        this.name = event.getName();
        this.description = event.getDescription();
    }
}
