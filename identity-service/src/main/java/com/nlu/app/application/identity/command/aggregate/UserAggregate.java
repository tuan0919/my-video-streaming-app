package com.nlu.app.application.identity.command.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.nlu.app.application.identity.command.AddRoleCommand;
import com.nlu.app.application.identity.command.CreateUserCommand;
import com.nlu.app.application.identity.command.LoginCommand;
import com.nlu.app.domain.identity.event.RoleAddedEvent;
import com.nlu.app.domain.identity.event.UserCreatedEvent;
import com.nlu.app.domain.authenticate.event.UserLoggedInEvent;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Aggregate
@RequiredArgsConstructor
@AllArgsConstructor
@Component
class UserAggregate {
    @AggregateIdentifier
    private String userId;

    @Autowired
    private transient PasswordEncoder passwordEncoder;

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
                .requestId(createUserCmd.getRequestId())
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
        // Verify password
        if (this.password.equals(loginCommand.getPassword())) {
            var event = UserLoggedInEvent.builder()
                    .userId(loginCommand.getUserId())
                    .username(loginCommand.getPassword())
                    .roles(new ArrayList<>(this.roles))
                    .build();
            apply(event);
        }
    }

    @EventSourcingHandler
    public void on(RoleAddedEvent event) {
        this.roles.add(event.getRoleName());
    }
}
