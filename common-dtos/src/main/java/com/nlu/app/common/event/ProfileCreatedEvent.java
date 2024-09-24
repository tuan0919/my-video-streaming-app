package com.nlu.app.common.event;

import com.nlu.app.common.dto.IdentityCreationDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ProfileCreatedEvent {
    private String eventId = UUID.randomUUID().toString();
    private IdentityCreationDTO identityCreationDTO;
    private Long timestamp = System.currentTimeMillis();
    private String sagaId;

    public ProfileCreatedEvent(IdentityCreationDTO userCreateDTO, String sagaId) {
        this.identityCreationDTO = identityCreationDTO;
        this.sagaId = sagaId;
    }
}
