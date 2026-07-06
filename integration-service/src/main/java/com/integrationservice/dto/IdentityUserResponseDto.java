package com.integrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityUserResponseDto {

    private UUID id;
    private String email;
    private String displayName;

    @JsonProperty("isVerified")
    private boolean verified;
}
