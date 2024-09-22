package com.nlu.app.commandSide.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nlu.app.commandSide.commands.AddRoleCommand;
import com.nlu.app.commandSide.commands.CreateUserCommand;
import com.nlu.app.commandSide.commands.LoginCommand;
import com.nlu.app.commandSide.service.UserStateService;
import com.nlu.app.domain.events.RoleAddedEvent;
import com.nlu.app.domain.events.UserCreatedEvent;
import com.nlu.app.domain.events.UserLoggedInEvent;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Aggregate
@RequiredArgsConstructor
@AllArgsConstructor
@Component
public class UserAggregate {
    @AggregateIdentifier
    private String userId;

    @Autowired
    private transient UserStateService service;

    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
    Set<String> roles;

    @CommandHandler
    public UserAggregate(CreateUserCommand createUserCmd) {
        var event = UserCreatedEvent.builder()
                .username(createUserCmd.getUsername())
                .email(createUserCmd.getEmail())
                .dob(createUserCmd.getDob())
                .city(createUserCmd.getCity())
                .firstName(createUserCmd.getFirstName())
                .lastName(createUserCmd.getLastName())
                .password(createUserCmd.getPassword())
                .userId(UUID.randomUUID().toString())
                .roles(createUserCmd.getRoles())
                .build();
        apply(event);
    }

    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        this.userId = event.getUserId();
        this.username = event.getUsername();
        this.password = event.getPassword();
        this.email = event.getEmail();
        this.city = event.getCity();
        this.firstName = event.getFirstName();
        this.lastName = event.getLastName();
        this.dob = event.getDob();
        this.roles = event.getRoles();
    }

    @CommandHandler
    public void addRoleHandler(AddRoleCommand cmd) {
        var roleName = cmd.getRoleName();
        //        var roleQuery = RoleExistsQuery.builder().name(roleName).build();
        //        try {
        //            if (!queryGateway.query(roleQuery, Boolean.class).join()) {
        //                throw new ApplicationException(ErrorCode.ROLE_NOT_EXISTED);
        //            }
        //        } catch (Exception e) {
        //            throw new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION);
        //        }
        var event = RoleAddedEvent.builder()
                .userId(cmd.getUserId())
                .roleName(roleName)
                .build();
        apply(event);
    }

    @CommandHandler
    public void handle(LoginCommand loginCommand) {
        var user = service.getUserByUsername(loginCommand.getUsername());
        // Verify password
        if (user.getPassword().equals(loginCommand.getPassword())) {
            var event = UserLoggedInEvent.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .build();
            apply(event);
        }
    }

    @EventSourcingHandler
    public void on(RoleAddedEvent event) {
        this.roles.add(event.getRoleName());
    }
}
