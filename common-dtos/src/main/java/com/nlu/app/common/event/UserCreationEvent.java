package com.nlu.app.common.event;

import com.nlu.app.common.dto.UserCreationDTO;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UserCreationEvent {
    private String eventId = UUID.randomUUID().toString();
    private UserCreationDTO userCreateDTO;
    private Long timestamp = System.currentTimeMillis();

    public UserCreationEvent(UserCreationDTO userCreateDTO) {
        this.userCreateDTO = userCreateDTO;
    }
}
