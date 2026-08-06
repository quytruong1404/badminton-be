package com.quy.badmintonbe.common.config;

import com.quy.badmintonbe.common.enums.UserRole;
import com.quy.badmintonbe.common.enums.UserStatus;
import com.quy.badmintonbe.common.exception.AppException;
import com.quy.badmintonbe.user.dto.UserDto;
import com.quy.badmintonbe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            if (uri.startsWith("/api/branches") ||
                uri.startsWith("/api/courts") ||
                uri.startsWith("/api/time-slots") ||
                uri.startsWith("/api/pricing") ||
                uri.startsWith("/api/products") ||
                uri.startsWith("/api/branch-inventories") ||
                uri.startsWith("/api/vouchers") ||
                uri.startsWith("/api/reviews") ||
                uri.startsWith("/api/system-configs") ||
                uri.startsWith("/api/cancellation-policies") ||
                uri.startsWith("/api/bookings/occupied-slots") ||
                uri.startsWith("/api/public/")) {
                return true;
            }
        }

        if (uri.startsWith("/api/auth/") || uri.startsWith("/api/payments/vnpay-callback")) {
            return true;
        }

        UserDto currentUser = null;

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            currentUser = (UserDto) session.getAttribute("currentUser");
        } else {
            
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdHeader.trim());
                    currentUser = userService.getUserById(userId);
                } catch (Exception ignored) {
                }
            }
        }

        if (currentUser == null) {
            throw new AppException("Yêu cầu đăng nhập trước khi thực hiện tác vụ.", 401);
        }

        if (UserStatus.LOCKED.equals(currentUser.getStatus())) {
            throw new AppException("Tài khoản của bạn hiện đang bị khóa. Không thể thực hiện tác vụ.", 403);
        }

        UserRole role = currentUser.getRole();

        if (uri.startsWith("/api/users")) {
            if ("GET".equalsIgnoreCase(method)) {
                if (role != UserRole.ADMIN && role != UserRole.MANAGER && role != UserRole.STAFF) {
                    throw new AppException("Không có quyền truy cập.", 403);
                }
            } else if (role != UserRole.ADMIN) {
                throw new AppException("Không có quyền truy cập. Tính năng này chỉ dành cho Admin hệ thống.", 403);
            }
        }

        if (uri.startsWith("/api/system-configs")) {
            if (role != UserRole.ADMIN) {
                throw new AppException("Không có quyền truy cập. Tính năng này chỉ dành cho Admin hệ thống.", 403);
            }
        }

        if (uri.startsWith("/api/branches") && ! "GET".equalsIgnoreCase(method)) {
            if (role != UserRole.ADMIN) {
                throw new AppException("Không có quyền thay đổi thông tin chi nhánh. Chỉ Admin mới được thực hiện.", 403);
            }
        }

        if (role == UserRole.MANAGER || role == UserRole.STAFF) {
            String branchIdParam = request.getParameter("branchId");
            if (branchIdParam != null && !branchIdParam.trim().isEmpty()) {
                try {
                    Long reqBranchId = Long.parseLong(branchIdParam.trim());
                    Long assignedBranchId = currentUser.getAssignedBranchId();
                    if (assignedBranchId == null || !assignedBranchId.equals(reqBranchId)) {
                        throw new AppException("Bạn không có quyền truy cập hoặc thao tác trên chi nhánh khác chi nhánh làm việc.", 403);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return true;
    }
}
