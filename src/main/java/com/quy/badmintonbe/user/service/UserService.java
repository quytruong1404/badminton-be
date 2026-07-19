package com.quy.badmintonbe.user.service;

import com.quy.badmintonbe.user.dto.UserDto;
import java.util.List;

public interface UserService {
    UserDto getUserById(Long id);
    UserDto getUserByEmail(String email);
    List<UserDto> getAllUsers();
    UserDto createUser(UserDto userDto);
    UserDto updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
    void changePassword(Long id, com.quy.badmintonbe.user.dto.ChangePasswordRequest request);
}
