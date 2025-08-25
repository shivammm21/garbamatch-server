package com.garba.garba.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    // Can be email or mobile number
    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
