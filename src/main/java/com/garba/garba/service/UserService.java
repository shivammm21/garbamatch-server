package com.garba.garba.service;

import com.garba.garba.dto.LoginRequest;
import com.garba.garba.dto.UserCreateRequest;
import com.garba.garba.dto.UserResponse;

public interface UserService {
    UserResponse register(UserCreateRequest request);
    UserResponse login(LoginRequest request);
}
