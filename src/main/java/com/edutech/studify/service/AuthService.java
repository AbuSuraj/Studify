package com.edutech.studify.service;

import com.edutech.studify.dto.request.ChangePasswordRequest;
import com.edutech.studify.dto.request.LoginRequest;
import com.edutech.studify.dto.request.RegisterRequest;
import com.edutech.studify.dto.response.AuthResponse;
import com.edutech.studify.dto.response.RegisterResponse;
import com.edutech.studify.dto.response.UserResponse;
import com.edutech.studify.entity.User;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

    void changePassword(ChangePasswordRequest request);
    UserResponse getUserProfile();
}