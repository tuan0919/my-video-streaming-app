package com.nlu.app.common.share.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class IdentityUpdatedEvent implements Serializable {
    String userId;
    String password;
    List<String> roles;
}
