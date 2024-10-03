package com.nlu.app.common.share.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppResponse<T> {
    @Builder.Default
    int code = 1000;
    String message;
    T result;
}
