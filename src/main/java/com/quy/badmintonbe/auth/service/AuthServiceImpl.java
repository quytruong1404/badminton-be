package com.quy.badmintonbe.auth.service;

import com.quy.badmintonbe.auth.dto.AuthResponse;
import com.quy.badmintonbe.auth.dto.LoginRequest;
import com.quy.badmintonbe.auth.dto.RegisterRequest;
import com.quy.badmintonbe.auth.dto.ForgotPasswordRequest;
import com.quy.badmintonbe.auth.dto.ResetPasswordRequest;
import com.quy.badmintonbe.common.enums.UserRole;
import com.quy.badmintonbe.common.enums.UserStatus;
import com.quy.badmintonbe.common.exception.BadRequestException;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.user.dto.UserDto;
import com.quy.badmintonbe.user.entity.User;
import com.quy.badmintonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + loginRequest.getEmail()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu không chính xác.");
        }

        UserDto userDto = mapToDto(user);
        return AuthResponse.builder()
                .token("mock-jwt-token-for-user-" + user.getId())
                .user(userDto)
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new BadRequestException("Email này đã được sử dụng.");
        }
        if (userRepository.findByPhoneNumber(registerRequest.getPhoneNumber()).isPresent()) {
            throw new BadRequestException("Số điện thoại này đã được sử dụng.");
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        UserDto userDto = mapToDto(savedUser);

        return AuthResponse.builder()
                .token("mock-jwt-token-for-user-" + savedUser.getId())
                .user(userDto)
                .build();
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này: " + request.getEmail()));
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!"123456".equals(request.getOtp())) {
            throw new BadRequestException("Mã xác thực OTP không chính xác.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + request.getEmail()));
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
