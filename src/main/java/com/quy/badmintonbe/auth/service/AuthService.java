package com.quy.badmintonbe.auth.service;

import com.quy.badmintonbe.auth.dto.AuthResponse;
import com.quy.badmintonbe.auth.dto.LoginRequest;
import com.quy.badmintonbe.auth.dto.RegisterRequest;
import com.quy.badmintonbe.auth.dto.ForgotPasswordRequest;
import com.quy.badmintonbe.auth.dto.ResetPasswordRequest;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
