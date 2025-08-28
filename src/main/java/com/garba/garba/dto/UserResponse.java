package com.garba.garba.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class UserResponse {
    Long id;
    String fullName;
    String email;
    String mobile;
    Integer age;
    String gender;
    String garbaSkill;
    String location;
    BigDecimal walletAmount;
    Long profilePicId;
    String bio;
    String planMode;
    String token;
}
