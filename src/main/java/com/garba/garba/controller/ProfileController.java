package com.garba.garba.controller;

import com.garba.garba.dto.UpdateProfileRequest;
import com.garba.garba.dto.UserResponse;
import com.garba.garba.service.UserService;
import com.garba.garba.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public ProfileController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Get user profile information using JWT token
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        
        if (!jwtUtil.isTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserResponse userProfile = userService.getUserProfile(userId);
        
        return ResponseEntity.ok(userProfile);
    }

    // Get user profile picture using JWT token
    @GetMapping("/picture")
    public ResponseEntity<byte[]> getProfilePicture(@RequestHeader("Authorization") String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        
        if (!jwtUtil.isTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserResponse userProfile = userService.getUserProfile(userId);
        
        if (userProfile.getProfilePicId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No profile picture found");
        }
        
        byte[] imageData = userService.getProfilePicture(userProfile.getProfilePicId());
        
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(imageData);
    }

    // Update user profile using JWT token (JSON)
    @PutMapping(value = "/update", consumes = "application/json")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        String token = extractTokenFromHeader(authHeader);
        
        if (!jwtUtil.isTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserResponse updatedProfile = userService.updateProfile(userId, request);
        
        return ResponseEntity.ok(updatedProfile);
    }

    // Update user profile using JWT token (Form Data with file)
    @PutMapping(value = "/update", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> updateProfileWithFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "age", required = false) String ageStr,
            @RequestParam(value = "garbaSkill", required = false) String garbaSkill,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        
        String token = extractTokenFromHeader(authHeader);
        
        if (!jwtUtil.isTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        
        try {
            // Debug logging
            System.out.println("Profile update request received:");
            System.out.println("fullName: " + fullName);
            System.out.println("bio: " + bio);
            System.out.println("profilePicture: " + (profilePicture != null ? profilePicture.getOriginalFilename() + " (" + profilePicture.getSize() + " bytes)" : "null"));
            
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFullName(fullName);
            request.setGarbaSkill(garbaSkill);
            request.setLocation(location);
            request.setBio(bio);
            
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
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserResponse updatedProfile = userService.updateProfileWithFile(userId, request, profilePicture);
            
            return ResponseEntity.ok(updatedProfile);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Profile update failed: " + e.getMessage());
        }
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header must start with 'Bearer '");
        }
        return authHeader.substring(7); // Remove "Bearer " prefix
    }
}