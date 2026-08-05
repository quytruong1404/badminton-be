package com.quy.badmintonbe.common.config;

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
        // 1. Cho phép các yêu cầu Preflight OPTIONS luôn được thông qua
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 2. Cho phép tất cả các truy vấn GET xem dữ liệu công khai không yêu cầu đăng nhập
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

        // 3. Đường dẫn Auth & Webhook công khai
        if (uri.startsWith("/api/auth/") || uri.startsWith("/api/payments/vnpay-callback")) {
            return true;
        }

        UserDto currentUser = null;

        // 4. Kiểm tra Session
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            currentUser = (UserDto) session.getAttribute("currentUser");
        } else {
            // 5. Kiểm tra Header X-User-Id
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

        // Kiểm tra vai trò Admin
        if (uri.contains("/admin/") && !"ADMIN".equals(currentUser.getRole().name())) {
            throw new AppException("Không có quyền truy cập. Yêu cầu quyền Admin.", 403);
        }

        return true;
    }
}
