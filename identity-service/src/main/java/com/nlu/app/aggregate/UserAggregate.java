package com.nlu.app.aggregate;

import java.time.LocalDate;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import com.nlu.app.commands.UserCreationCommand;
import com.nlu.app.events.UserCreateEvent;
import com.nlu.app.handler.UserEventsHandler;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Aggregate
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class UserAggregate {
    @AggregateIdentifier
    private String userId;

    UserEventsHandler userEventsHandler;

    @CommandHandler
    public UserAggregate(UserCreationCommand command) {
        // publish event
        UserCreateEvent event = UserCreateEvent.builder()
                .dob(LocalDate.now())
                .email(command.getEmail())
                .city(command.getCity())
                .role(command.getRole())
                .lastName(command.getLastName())
                .firstName(command.getFirstName())
                .username(command.getUsername())
                .password(command.getPassword())
                .build();
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public boolean on(UserCreateEvent event) {
        return false;
    }
}
