package com.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginEmailRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
