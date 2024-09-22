package com.nlu.app.domain.query;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PermissionsExistsQuery {
    Set<String> names;
}
