package com.nlu.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import com.nlu.app.common.axon.UserCreatedEvent;
import com.nlu.app.common.axon.UserCreationCommand;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Aggregate
public class User {
    @Id
    @AggregateIdentifier
    String id;

    @Column(name = "username", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;

    String password;

    @Column(name = "email", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String email;

    @Column(name = "email_verified", nullable = false, columnDefinition = "boolean default false")
    boolean emailVerified;

    @ManyToMany
    Set<Role> roles = new HashSet<>();

    @CommandHandler
    public User(UserCreationCommand creationCommand) {
        this.id = creationCommand.getId();
        this.username = creationCommand.getUsername();
        this.password = creationCommand.getPassword();
        this.email = creationCommand.getEmail();
        this.emailVerified = creationCommand.getVerified();

        var event = UserCreatedEvent.builder()
                .verified(creationCommand.getVerified())
                .email(creationCommand.getEmail())
                .password(creationCommand.getPassword())
                .username(creationCommand.getUsername())
                .userId(this.id)
                .build();
        AggregateLifecycle.apply(event);
    }
}
