package com.nlu.app.common.event;

import com.nlu.app.common.dto.UserCreationDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ProfileCreatedEvent {
    private String eventId = UUID.randomUUID().toString();
    private UserCreationDTO userCreateDTO;
    private Long timestamp = System.currentTimeMillis();
    private String sagaId;

    public ProfileCreatedEvent(UserCreationDTO userCreateDTO, String sagaId) {
        this.userCreateDTO = userCreateDTO;
        this.sagaId = sagaId;
    }
}
