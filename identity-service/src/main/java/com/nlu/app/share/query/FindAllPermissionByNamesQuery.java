package com.nlu.app.share.query;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FindAllPermissionByNamesQuery {
    Set<String> names;
}
