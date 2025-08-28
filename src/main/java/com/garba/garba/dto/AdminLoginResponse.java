package com.garba.garba.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminLoginResponse {
    String username;
    String role;
    String token;
    String message;
}