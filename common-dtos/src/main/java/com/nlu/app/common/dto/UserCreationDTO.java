package com.nlu.app.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreationDTO {
    private String userId;
    private Long timestamp;
    private String username;
}
