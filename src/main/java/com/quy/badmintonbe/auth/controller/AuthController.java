package com.quy.badmintonbe.auth.controller;

import com.quy.badmintonbe.auth.dto.AuthResponse;
import com.quy.badmintonbe.auth.dto.LoginRequest;
import com.quy.badmintonbe.auth.dto.RegisterRequest;
import com.quy.badmintonbe.auth.dto.ForgotPasswordRequest;
import com.quy.badmintonbe.auth.dto.ResetPasswordRequest;
import com.quy.badmintonbe.auth.service.AuthService;
import com.quy.badmintonbe.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        AuthResponse authResponse = authService.login(loginRequest);
        
        // Store user DTO in session
        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", authResponse.getUser());
        
        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Đăng nhập thành công.")
                .data(authResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {
        
        AuthResponse authResponse = authService.register(registerRequest);
        
        // Store registered user DTO in session
        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", authResponse.getUser());
        
        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Đăng ký tài khoản thành công.")
                .data(authResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Đăng xuất thành công.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authService.forgotPassword(forgotPasswordRequest);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Mã xác thực OTP demo là 123456")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Đặt lại mật khẩu thành công.")
                .build();
        return ResponseEntity.ok(response);
    }
}
