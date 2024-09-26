package com.nlu.app.saga;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nlu.app.common.axon.NotificationCreatedEvent;
import com.nlu.app.common.axon.NotificationCreationCommand;
import com.nlu.app.common.axon.UserCreatedEvent;

@Saga
@Component
public class UserCreationSaga {
    private transient CommandGateway commandGateway;

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "userId")
    public void handle(UserCreatedEvent event) {
        String notificationId = UUID.randomUUID().toString();
        SagaLifecycle.associateWith("notificationId", notificationId);
        var notificationCommand = NotificationCreationCommand.builder()
                .userId(event.getUserId())
                .notificationId(notificationId)
                .content("Welcome user " + event.getUsername())
                .build();
        commandGateway.send(notificationCommand);
    }

    @SagaEventHandler(associationProperty = "notificationId")
    @EndSaga
    public void handle(NotificationCreatedEvent event) {
        // Process notification
        System.out.println("Notificaton Created");
    }
}
