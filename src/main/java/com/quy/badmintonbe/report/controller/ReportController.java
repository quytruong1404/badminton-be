package com.quy.badmintonbe.report.controller;

import com.quy.badmintonbe.common.enums.UserRole;
import com.quy.badmintonbe.common.exception.AppException;
import com.quy.badmintonbe.report.dto.DashboardReportDto;
import com.quy.badmintonbe.report.service.ReportService;
import com.quy.badmintonbe.user.dto.UserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReportDto> getDashboardReport(HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new AppException("Yêu cầu đăng nhập trước khi thực hiện tác vụ.", 401);
        }

        // Only allow ADMIN, MANAGER, STAFF roles to view reports
        if (currentUser.getRole() == UserRole.CUSTOMER) {
            throw new AppException("Bạn không có quyền xem báo cáo thống kê này.", 403);
        }

        DashboardReportDto report = reportService.getDashboardReport();
        return ResponseEntity.ok(report);
    }
}
