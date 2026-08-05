package com.quy.badmintonbe.user.service;

import com.quy.badmintonbe.branch.entity.Branch;
import com.quy.badmintonbe.branch.entity.StaffBranch;
import com.quy.badmintonbe.branch.repository.BranchRepository;
import com.quy.badmintonbe.branch.repository.StaffBranchRepository;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.user.dto.UserDto;
import com.quy.badmintonbe.user.entity.User;
import com.quy.badmintonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StaffBranchRepository staffBranchRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return mapToDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));
        return mapToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = mapToEntity(userDto);
        // Thiết lập mật khẩu mặc định (được mã hóa) cho tài khoản mới do Admin tạo
        user.setPassword(passwordEncoder.encode("123456"));
        User savedUser = userRepository.save(user);

        if (userDto.getAssignedBranchId() != null && userDto.getAssignedBranchId() > 0) {
            updateStaffBranchAssignment(savedUser, userDto.getAssignedBranchId());
        }

        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        
        // Ngăn khóa tài khoản Admin duy nhất
        if (com.quy.badmintonbe.common.enums.UserRole.ADMIN.equals(user.getRole()) && com.quy.badmintonbe.common.enums.UserStatus.LOCKED.equals(userDto.getStatus())) {
            long activeAdminCount = userRepository.findAll().stream()
                    .filter(u -> com.quy.badmintonbe.common.enums.UserRole.ADMIN.equals(u.getRole()) && com.quy.badmintonbe.common.enums.UserStatus.ACTIVE.equals(u.getStatus()))
                    .count();
            if (activeAdminCount <= 1) {
                throw new com.quy.badmintonbe.common.exception.BadRequestException("Không thể khóa tài khoản Quản trị viên (Admin) duy nhất đang hoạt động của hệ thống.");
            }
        }

        user.setFullName(userDto.getFullName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        if (userDto.getRole() != null) {
            user.setRole(userDto.getRole());
        }
        if (userDto.getStatus() != null) {
            user.setStatus(userDto.getStatus());
        }

        User updatedUser = userRepository.save(user);

        // Cập nhật liên kết phân công chi nhánh cho nhân viên / quản lý trong bảng staff_branches
        updateStaffBranchAssignment(updatedUser, userDto.getAssignedBranchId());

        return mapToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        
        // Ngăn xóa tài khoản Admin duy nhất
        if (com.quy.badmintonbe.common.enums.UserRole.ADMIN.equals(user.getRole())) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> com.quy.badmintonbe.common.enums.UserRole.ADMIN.equals(u.getRole()))
                    .count();
            if (adminCount <= 1) {
                throw new com.quy.badmintonbe.common.exception.BadRequestException("Không thể xóa tài khoản Quản trị viên (Admin) duy nhất của hệ thống.");
            }
        }

        // Xóa bản ghi phân công chi nhánh trước khi xóa người dùng
        List<StaffBranch> staffBranches = staffBranchRepository.findByUserId(id);
        if (!staffBranches.isEmpty()) {
            staffBranchRepository.deleteAll(staffBranches);
        }

        userRepository.delete(user);
    }

    @Override
    public void changePassword(Long id, com.quy.badmintonbe.user.dto.ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new com.quy.badmintonbe.common.exception.BadRequestException("Mật khẩu cũ không chính xác.");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private void updateStaffBranchAssignment(User user, Long assignedBranchId) {
        List<StaffBranch> existing = staffBranchRepository.findByUserId(user.getId());

        if (assignedBranchId == null || assignedBranchId <= 0) {
            if (!existing.isEmpty()) {
                staffBranchRepository.deleteAll(existing);
            }
            return;
        }

        Branch branch = branchRepository.findById(assignedBranchId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + assignedBranchId));

        if (existing.isEmpty()) {
            StaffBranch sb = StaffBranch.builder()
                    .user(user)
                    .branch(branch)
                    .build();
            staffBranchRepository.save(sb);
        } else {
            StaffBranch sb = existing.get(0);
            sb.setBranch(branch);
            staffBranchRepository.save(sb);
            if (existing.size() > 1) {
                staffBranchRepository.deleteAll(existing.subList(1, existing.size()));
            }
        }
    }

    private UserDto mapToDto(User user) {
        Long assignedBranchId = null;
        String assignedBranchName = null;

        List<StaffBranch> staffBranches = staffBranchRepository.findByUserId(user.getId());
        if (!staffBranches.isEmpty()) {
            StaffBranch sb = staffBranches.get(0);
            assignedBranchId = sb.getBranch().getId();
            assignedBranchName = sb.getBranch().getName();
        }

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .assignedBranchId(assignedBranchId)
                .assignedBranchName(assignedBranchName)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private User mapToEntity(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .status(dto.getStatus())
                .build();
    }
}
