package com.quy.badmintonbe.ai.controller;

import com.quy.badmintonbe.booking.entity.Booking;
import com.quy.badmintonbe.booking.entity.BookingDetail;
import com.quy.badmintonbe.booking.entity.BookingServiceItem;
import com.quy.badmintonbe.booking.entity.CourtReservation;
import com.quy.badmintonbe.booking.repository.BookingDetailRepository;
import com.quy.badmintonbe.booking.repository.BookingRepository;
import com.quy.badmintonbe.booking.repository.BookingServiceRepository;
import com.quy.badmintonbe.booking.repository.CourtReservationRepository;
import com.quy.badmintonbe.common.enums.DayType;
import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.branch.entity.Branch;
import com.quy.badmintonbe.court.entity.Court;
import com.quy.badmintonbe.court.entity.TimeSlot;
import com.quy.badmintonbe.branch.repository.BranchRepository;
import com.quy.badmintonbe.court.repository.CourtRepository;
import com.quy.badmintonbe.court.repository.TimeSlotRepository;
import com.quy.badmintonbe.pricing.entity.PricingRule;
import com.quy.badmintonbe.pricing.repository.PricingRuleRepository;
import com.quy.badmintonbe.product.entity.Product;
import com.quy.badmintonbe.product.repository.ProductRepository;
import com.quy.badmintonbe.voucher.entity.Voucher;
import com.quy.badmintonbe.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final BranchRepository branchRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final CourtReservationRepository courtReservationRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final BookingServiceRepository bookingServiceRepository;

    // 1. API Chi nhánh: GET /api/public/ai/branches
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBranches() {
        List<Branch> branches = branchRepository.findAll();
        List<Map<String, Object>> data = branches.stream().map(b -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("branchId", b.getId());
            map.put("name", b.getName());
            map.put("address", b.getAddress());
            map.put("phoneNumber", b.getPhoneNumber());
            map.put("openTime", b.getOpenTime().toString());
            map.put("closeTime", b.getCloseTime().toString());
            map.put("status", b.getStatus().toString());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("AI branches retrieved successfully")
                .data(data)
                .build());
    }

    // 2. API Lịch trống sân: GET /api/public/ai/available-courts?date=YYYY-MM-DD
    @GetMapping("/available-courts")
    public ResponseEntity<ApiResponse<Object>> getAvailableCourts(@RequestParam String date) {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .success(false)
                    .message("Định dạng ngày không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.")
                    .build());
        }

        // Xác định DayType (WEEKDAY hoặc WEEKEND)
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
        DayType dayType = isWeekend ? DayType.WEEKEND : DayType.WEEKDAY;

        List<Court> courts = courtRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findAll().stream()
                .filter(s -> "ACTIVE".equals(s.getStatus().toString()))
                .sorted(Comparator.comparing(TimeSlot::getStartTime))
                .collect(Collectors.toList());

        List<CourtReservation> reservations = courtReservationRepository.findByReservationDate(localDate).stream()
                .filter(r -> "ACTIVE".equals(r.getStatus().toString()) || "COMPLETED".equals(r.getStatus().toString()))
                .collect(Collectors.toList());

        List<PricingRule> pricingRules = pricingRuleRepository.findAll().stream()
                .filter(pr -> "ACTIVE".equals(pr.getStatus().toString()))
                .collect(Collectors.toList());

        List<Map<String, Object>> courtsData = new ArrayList<>();

        for (Court court : courts) {
            Map<String, Object> courtMap = new LinkedHashMap<>();
            courtMap.put("courtId", court.getId());
            courtMap.put("courtName", court.getName());
            courtMap.put("branchName", court.getBranch().getName());
            courtMap.put("status", court.getStatus().toString());

            // Lấy các reservations của riêng sân này
            List<CourtReservation> courtReservations = reservations.stream()
                    .filter(r -> r.getCourt().getId().equals(court.getId()))
                    .collect(Collectors.toList());

            List<String> occupiedSlots = new ArrayList<>();
            for (CourtReservation r : courtReservations) {
                occupiedSlots.add(r.getSlot().getStartTime().toString().substring(0, 5) + " - " + r.getSlot().getEndTime().toString().substring(0, 5));
            }

            int availableSlotsCount = slots.size() - occupiedSlots.size();

            // Lấy khung giá ngày thường và cuối tuần của sân để AI tư vấn giá
            BigDecimal priceWeekday = pricingRules.stream()
                    .filter(pr -> pr.getCourt().getId().equals(court.getId()) && pr.getDayType() == DayType.WEEKDAY)
                    .map(PricingRule::getPrice)
                    .findFirst()
                    .orElse(BigDecimal.valueOf(50000.0));

            BigDecimal priceWeekend = pricingRules.stream()
                    .filter(pr -> pr.getCourt().getId().equals(court.getId()) && pr.getDayType() == DayType.WEEKEND)
                    .map(PricingRule::getPrice)
                    .findFirst()
                    .orElse(BigDecimal.valueOf(65000.0));

            courtMap.put("priceWeekday", priceWeekday);
            courtMap.put("priceWeekend", priceWeekend);
            courtMap.put("totalSlotsCount", slots.size());
            courtMap.put("availableSlotsCount", availableSlotsCount);
            courtMap.put("occupiedSlots", occupiedSlots); // Chỉ liệt kê ca đã bận, các ca còn lại tự hiểu là trống

            courtsData.add(courtMap);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("date", date);
        summary.put("dayOfWeek", dayOfWeek.toString());
        summary.put("dayType", dayType.toString());
        summary.put("totalCourts", courts.size());

        List<Map<String, Object>> occupiedSlotsDetail = new ArrayList<>();
        for (CourtReservation r : reservations) {
            Map<String, Object> occDetail = new LinkedHashMap<>();
            occDetail.put("courtName", r.getCourt().getName());
            occDetail.put("branchName", r.getCourt().getBranch().getName());
            occDetail.put("slotTime", r.getSlot().getStartTime().toString().substring(0, 5) + " - " + r.getSlot().getEndTime().toString().substring(0, 5));
            occupiedSlotsDetail.add(occDetail);
        }

        int totalOccupiedSlots = reservations.size();
        int totalAvailableSlots = (courts.size() * slots.size()) - totalOccupiedSlots;

        summary.put("totalOccupiedSlots", totalOccupiedSlots);
        summary.put("totalAvailableSlots", totalAvailableSlots);
        summary.put("occupiedSlotsDetail", occupiedSlotsDetail);

        response.put("summary", summary);
        response.put("courts", courtsData);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("AI available courts retrieved successfully")
                .data(response)
                .build());
    }

    // 3. API Dịch vụ đi kèm: GET /api/public/ai/products
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProducts() {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> "ACTIVE".equals(p.getStatus().toString()))
                .collect(Collectors.toList());

        List<Map<String, Object>> data = products.stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("productId", p.getId());
            map.put("name", p.getName());
            map.put("type", p.getProductType().toString()); // SELL / RENT
            map.put("unit", p.getUnit()); // chai, cay, doi...
            map.put("chargeType", p.getChargeType().toString()); // PER_UNIT / PER_SLOT
            map.put("price", p.getPrice());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("AI products retrieved successfully")
                .data(data)
                .build());
    }

    // 4. API Khuyến mãi: GET /api/public/ai/vouchers
    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll().stream()
                .filter(v -> "ACTIVE".equals(v.getStatus().toString()) && v.getUsedCount() < v.getUsageLimit())
                .collect(Collectors.toList());

        List<Map<String, Object>> data = vouchers.stream().map(v -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("code", v.getCode());
            map.put("discountType", v.getDiscountType().toString()); // PERCENT / AMOUNT
            map.put("discountValue", v.getDiscountValue());
            map.put("minOrderValue", v.getMinOrderValue());
            map.put("maxDiscount", v.getMaxDiscount());
            map.put("endDate", v.getEndDate().toString());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("AI vouchers retrieved successfully")
                .data(data)
                .build());
    }

    // 5. API Tra cứu đơn đặt: GET /api/public/ai/booking-lookup?bookingCode=BK-...
    @GetMapping("/booking-lookup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> lookupBooking(@RequestParam String bookingCode) {
        Optional<Booking> bookingOpt = bookingRepository.findByBookingCode(bookingCode);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Không tìm thấy đơn đặt sân nào với mã: " + bookingCode)
                    .build());
        }

        Booking booking = bookingOpt.get();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bookingCode", booking.getBookingCode());
        data.put("customerName", booking.getUser().getFullName());
        data.put("totalPrice", booking.getTotalPrice());
        data.put("bookingStatus", booking.getBookingStatus().toString()); // PENDING, CONFIRMED, COMPLETED, CANCELLED
        data.put("paymentStatus", booking.getPaymentStatus().toString()); // UNPAID, PAID, REFUNDED...
        data.put("createdAt", booking.getCreatedAt().toString());

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(booking.getId());
        List<Map<String, Object>> details = bookingDetails.stream().map(d -> {
            Map<String, Object> dMap = new LinkedHashMap<>();
            dMap.put("branchName", d.getCourt().getBranch().getName());
            dMap.put("courtName", d.getCourt().getName());
            dMap.put("bookingDate", d.getBookingDate().toString());
            dMap.put("timeSlot", d.getSlot().getStartTime().toString().substring(0, 5) + " - " + d.getSlot().getEndTime().toString().substring(0, 5));
            dMap.put("unitPrice", d.getUnitPrice());
            dMap.put("status", d.getDetailStatus().toString());
            return dMap;
        }).collect(Collectors.toList());

        data.put("details", details);

        List<BookingServiceItem> serviceItems = bookingServiceRepository.findByBookingId(booking.getId());
        List<Map<String, Object>> services = serviceItems.stream().map(s -> {
            Map<String, Object> sMap = new LinkedHashMap<>();
            sMap.put("productName", s.getProduct().getName());
            sMap.put("quantity", s.getQuantity());
            sMap.put("unitPrice", s.getUnitPrice());
            sMap.put("totalPrice", s.getTotalPrice());
            return sMap;
        }).collect(Collectors.toList());

        data.put("services", services);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("AI booking lookup successfully")
                .data(data)
                .build());
    }
}
