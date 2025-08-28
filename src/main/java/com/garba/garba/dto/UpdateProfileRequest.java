package com.garba.garba.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 120)
    private String fullName;

    @Min(0)
    @Max(120)
    private Integer age;

    @Size(max = 50)
    private String garbaSkill;

    @Size(max = 120)
    private String location;

    @Size(max = 500)
    private String bio;

    private String profilePicture; // Base64 encoded image data (optional)
}