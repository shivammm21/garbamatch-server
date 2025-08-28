package com.garba.garba.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank
    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 180)
    private String email;

    @NotBlank
    @Size(min = 6, max = 20)
    private String mobile;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password; // plain input, will be hashed

    @Min(0)
    @Max(120)
    private Integer age; // optional

    @Size(max = 20)
    private String gender;

    @Size(max = 50)
    private String garbaSkill;

    @Size(max = 120)
    private String location;

    private String profilePicture; // Base64 encoded image data

    @Size(max = 500)
    private String bio; // User biography/description
}
