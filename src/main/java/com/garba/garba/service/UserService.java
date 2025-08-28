package com.garba.garba.service;

import com.garba.garba.dto.LoginRequest;
import com.garba.garba.dto.UpdateProfileRequest;
import com.garba.garba.dto.UserCreateRequest;
import com.garba.garba.dto.UserResponse;
import com.garba.garba.dto.UserStatsResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse register(UserCreateRequest request);
    UserResponse registerWithFile(UserCreateRequest request, MultipartFile profilePic);
    UserResponse login(LoginRequest request);
    UserResponse getUserProfile(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserResponse updateProfileWithFile(Long userId, UpdateProfileRequest request, MultipartFile profilePic);
    byte[] getProfilePicture(Long imageId);
    UserStatsResponse getUserStats();
}
