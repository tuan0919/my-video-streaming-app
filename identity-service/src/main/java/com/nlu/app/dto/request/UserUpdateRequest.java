package com.nlu.app.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.nlu.app.entity.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    List<String> roles;
}
