package com.garba.garba.controller;

import com.garba.garba.dto.LoginRequest;
import com.garba.garba.dto.UserCreateRequest;
import com.garba.garba.dto.UserResponse;
import com.garba.garba.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create user with multipart form data (for file uploads)
    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> registerWithFile(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("mobile") String mobile,
            @RequestParam(value = "age", required = false) String ageStr,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "garbaSkill", required = false) String garbaSkill,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "profilePic", required = false) MultipartFile profilePic) {
        
        try {
            // Basic validation
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
            }
            if (mobile == null || mobile.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile is required");
            }
            
            // Validate password length
            if (password.trim().length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters long");
            }
            
            // Validate mobile length
            if (mobile.trim().length() < 6 || mobile.trim().length() > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile number must be between 6 and 20 characters");
            }
            
            UserCreateRequest request = new UserCreateRequest();
            request.setFullName(fullName.trim());
            request.setEmail(email.trim());
            request.setPassword(password.trim());
            request.setMobile(mobile.trim());
            
            // Parse age if provided
            if (ageStr != null && !ageStr.trim().isEmpty()) {
                try {
                    int age = Integer.parseInt(ageStr.trim());
                    if (age < 0 || age > 120) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be between 0 and 120");
                    }
                    request.setAge(age);
                } catch (NumberFormatException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid age format");
                }
            }
            
            request.setGender(gender);
            request.setGarbaSkill(garbaSkill);
            request.setLocation(location);
            request.setBio(bio);
            
            UserResponse created = userService.registerWithFile(request, profilePic);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
        } catch (ResponseStatusException e) {
            throw e; // Re-throw ResponseStatusException as-is
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed: " + e.getMessage());
        }
    }

    // Create user: inputs fullName, email, mobile, password, age, gender, garbaSkill, location (JSON)
    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Login by email-id or mobile-no + password
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse user = userService.login(request);
        return ResponseEntity.ok(user);
    }

    // Get profile picture by image ID
    @GetMapping("/profile-picture/{imageId}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long imageId) {
        byte[] imageData = userService.getProfilePicture(imageId);
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(imageData);
    }
}
