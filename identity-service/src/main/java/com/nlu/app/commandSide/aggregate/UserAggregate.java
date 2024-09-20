package com.nlu.app.commandSide.aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nlu.app.commandSide.commands.AddRoleCommand;
import com.nlu.app.commandSide.commands.CreateUserCommand;
import com.nlu.app.querySide.exception.ApplicationException;
import com.nlu.app.querySide.exception.ErrorCode;
import com.nlu.app.share.events.RoleAddedEvent;
import com.nlu.app.share.events.UserCreatedEvent;
import com.nlu.app.share.query.EmailExistsQuery;
import com.nlu.app.share.query.RoleExistsQuery;
import com.nlu.app.share.query.UsernameExistsQuery;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Aggregate
@RequiredArgsConstructor
@AllArgsConstructor
@Component
public class UserAggregate {
    @AggregateIdentifier
    private String userId;

    private QueryGateway queryGateway;

    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
    Set<String> roles;

    @Autowired
    public void setQueryGateway(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand createUserCmd) {
        String username = createUserCmd.getUsername();
        String email = createUserCmd.getEmail();
        var queryUsername = UsernameExistsQuery.builder().username(username).build();
        var queryEmail = EmailExistsQuery.builder().email(email).build();
        try {
            if (queryGateway.query(queryUsername, Boolean.class).join()) {
                throw new ApplicationException(ErrorCode.USER_ALREADY_EXISTED);
            }
            if (queryGateway.query(queryEmail, Boolean.class).join()) {
                throw new ApplicationException(ErrorCode.USER_ALREADY_EXISTED);
            }
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION);
        }
        var event = UserCreatedEvent.builder()
                .username(username)
                .email(email)
                .dob(createUserCmd.getDob())
                .city(createUserCmd.getCity())
                .firstName(createUserCmd.getFirstName())
                .lastName(createUserCmd.getLastName())
                .password(createUserCmd.getPassword())
                .userId(UUID.randomUUID().toString())
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
        this.roles = Set.of();
    }

    @CommandHandler
    public void addRoleHandler(AddRoleCommand cmd) {
        var roleName = cmd.getRoleName();
        var roleQuery = RoleExistsQuery.builder().name(roleName).build();
        try {
            if (!queryGateway.query(roleQuery, Boolean.class).join()) {
                throw new ApplicationException(ErrorCode.ROLE_NOT_EXISTED);
            }
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION);
        }
        var event = RoleAddedEvent.builder()
                .userId(cmd.getUserId())
                .roleName(roleName)
                .build();
        apply(event);
    }

    @EventSourcingHandler
    public void on(RoleAddedEvent event) {
        this.roles.add(event.getRoleName());
    }
}
