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
        // Cho phép các yêu cầu Preflight OPTIONS luôn được thông qua
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Cho phép xem đánh giá (GET /api/reviews) công khai không cần đăng nhập
        if (uri.startsWith("/api/reviews") && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        // Cho phép các API công khai của AI Assistant được thông qua
        if (uri.startsWith("/api/public/ai/")) {
            return true;
        }

        UserDto currentUser = null;

        // 1. Kiểm tra Session cũ
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            currentUser = (UserDto) session.getAttribute("currentUser");
        } else {
            // 2. Nếu Session bị chặn bởi Cross-Domain cookie (Vercel -> Render), kiểm tra header X-User-Id
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

        // Kiểm tra vai trò cơ bản cho các đường dẫn quản trị (admin)
        if (uri.contains("/admin/") && !"ADMIN".equals(currentUser.getRole().name())) {
            throw new AppException("Không có quyền truy cập. Yêu cầu quyền Admin.", 403);
        }

        return true;
    }
}
