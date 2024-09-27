package com.nlu.app.common.axon.command;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProfileCreationCommand {
    String profileId;
    String userId;
    String fullName;
    String country;
}
