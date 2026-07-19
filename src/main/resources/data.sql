-- =========================================================
-- INITIAL SEED DATA FOR BADMINTON COURT BOOKING SYSTEM
-- Uses INSERT IGNORE to protect user modifications in DB
-- =========================================================

-- 0. Clean up orphaned pricing rules from previous incorrect deletions to fix API crash
DELETE FROM `pricing_rules` WHERE `slot_id` NOT IN (SELECT `id` FROM `time_slots`);
DELETE FROM `pricing_rules` WHERE `court_id` NOT IN (SELECT `id` FROM `courts`);

-- Drop old Hibernate-generated non-cascade constraints if they exist (errors are ignored safely)
ALTER TABLE `pricing_rules` DROP FOREIGN KEY `FKswarijc1mlt687gd5wnwkef70`;
ALTER TABLE `pricing_rules` DROP FOREIGN KEY `FKhcywioeupn6i93lkh4v5xbecl`;
ALTER TABLE `pricing_rules` DROP FOREIGN KEY `fk_pricing_slot_cascade`;
ALTER TABLE `pricing_rules` DROP FOREIGN KEY `fk_pricing_court_cascade`;

-- Force recreate proper CASCADE foreign key constraints at database level
ALTER TABLE `pricing_rules` ADD CONSTRAINT `fk_pricing_slot_cascade` FOREIGN KEY (`slot_id`) REFERENCES `time_slots` (`id`) ON DELETE CASCADE;
ALTER TABLE `pricing_rules` ADD CONSTRAINT `fk_pricing_court_cascade` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`) ON DELETE CASCADE;

-- 1. Seed System Configuration
INSERT IGNORE INTO `system_configs` (`id`, `config_key`, `config_value`, `description`) VALUES
(1, 'TIMEOUT_MINS', '15', 'Thời gian giữ chỗ tạm thời trước khi thanh toán (phút)'),
(2, 'MAX_BOOKING_DAYS_ADVANCE', '7', 'Số ngày tối đa được đặt trước');

-- 2. Seed Default Users (Password: admin123 / customer123)
-- In production, these passwords should be BCrypt hashed.
INSERT IGNORE INTO `users` (`id`, `email`, `phone_number`, `password`, `full_name`, `role`, `status`) VALUES
(1, 'admin@gmail.com', '0123456789', '$2a$10$hqzCXOgCexwl.xL0qCB7e.zP4NKUMyXYnhUqyJJdXfHrQRXMcSihG', 'Quản trị viên Hệ thống', 'ADMIN', 'ACTIVE'),
(2, 'manager@gmail.com', '0987654321', '$2a$10$hqzCXOgCexwl.xL0qCB7e.zP4NKUMyXYnhUqyJJdXfHrQRXMcSihG', 'Quản lý Chi nhánh', 'MANAGER', 'ACTIVE'),
(3, 'staff@gmail.com', '0111222333', '$2a$10$hqzCXOgCexwl.xL0qCB7e.zP4NKUMyXYnhUqyJJdXfHrQRXMcSihG', 'Nhân viên Sân', 'STAFF', 'ACTIVE'),
(4, 'customer@gmail.com', '0999888777', '$2a$10$q8yBkWgMk/CuuL3vWwYtAOzBb2Mdex4S4Dvk12x4D3Kaica2IrbJa', 'Khách hàng Demo', 'CUSTOMER', 'ACTIVE');

-- 3. Seed Branches
INSERT IGNORE INTO `branches` (`id`, `name`, `address`, `phone_number`, `open_time`, `close_time`, `status`) VALUES
(1, 'Chi nhánh Long Khánh', '123 Hùng Vương, TP. Long Khánh', '02513123456', '05:00:00', '23:00:00', 'ACTIVE'),
(2, 'Chi nhánh Biên Hòa', '456 Nguyễn Ái Quốc, TP. Biên Hòa', '02513654321', '05:00:00', '23:00:00', 'ACTIVE'),
(3, 'Chi nhánh Trảng Dài', '789 Bùi Trọng Nghĩa, TP. Biên Hòa', '02513987654', '05:00:00', '23:00:00', 'ACTIVE'),
(4, 'Chi nhánh Hố Nai', '102 Nguyễn Ái Quốc, TP. Biên Hòa', '02513456789', '05:00:00', '22:00:00', 'ACTIVE');

-- 4. Seed Time Slots (Standard hourly slots from 05:00 to 23:00)
INSERT IGNORE INTO `time_slots` (`id`, `start_time`, `end_time`, `status`) VALUES
(1, '05:00:00', '06:00:00', 'ACTIVE'),
(2, '06:00:00', '07:00:00', 'ACTIVE'),
(3, '07:00:00', '08:00:00', 'ACTIVE'),
(4, '08:00:00', '09:00:00', 'ACTIVE'),
(5, '09:00:00', '10:00:00', 'ACTIVE'),
(6, '10:00:00', '11:00:00', 'ACTIVE'),
(7, '11:00:00', '12:00:00', 'ACTIVE'),
(8, '12:00:00', '13:00:00', 'ACTIVE'),
(9, '13:00:00', '14:00:00', 'ACTIVE'),
(10, '14:00:00', '15:00:00', 'ACTIVE'),
(11, '15:00:00', '16:00:00', 'ACTIVE'),
(12, '16:00:00', '17:00:00', 'ACTIVE'),
(13, '17:00:00', '18:00:00', 'ACTIVE'),
(14, '18:00:00', '19:00:00', 'ACTIVE'),
(15, '19:00:00', '20:00:00', 'ACTIVE'),
(16, '20:00:00', '21:00:00', 'ACTIVE'),
(17, '21:00:00', '22:00:00', 'ACTIVE'),
(18, '22:00:00', '23:00:00', 'ACTIVE');

-- 5. Seed Courts for Branches
INSERT IGNORE INTO `courts` (`id`, `branch_id`, `name`, `description`, `status`) VALUES
(1, 1, 'Sân Số 1 (Long Khánh)', 'Thảm Yonex chất lượng cao, ánh sáng tiêu chuẩn', 'AVAILABLE'),
(2, 1, 'Sân Số 2 (Long Khánh)', 'Thảm Yonex chất lượng cao, ánh sáng tiêu chuẩn', 'AVAILABLE'),
(3, 1, 'Sân Số 3 (Long Khánh)', 'Thảm Yonex chất lượng cao, ánh sáng tiêu chuẩn', 'AVAILABLE'),
(4, 2, 'Sân Số 1 (Biên Hòa)', 'Thảm Victor chống trượt, ánh sáng LED dịu mắt', 'AVAILABLE'),
(5, 2, 'Sân Số 2 (Biên Hòa)', 'Thảm Victor chống trượt, ánh sáng LED dịu mắt', 'AVAILABLE'),
(6, 1, 'Sân Số 4 (Long Khánh)', 'Thảm Yonex chất lượng cao, ánh sáng tiêu chuẩn', 'AVAILABLE'),
(7, 1, 'Sân Số 5 (Long Khánh)', 'Thảm Yonex chất lượng cao, ánh sáng tiêu chuẩn', 'AVAILABLE'),
(8, 2, 'Sân Số 3 (Biên Hòa)', 'Thảm Victor chống trượt, ánh sáng LED dịu mắt', 'AVAILABLE'),
(9, 2, 'Sân Số 4 (Biên Hòa)', 'Thảm Victor chống trượt, ánh sáng LED dịu mắt', 'AVAILABLE'),
(10, 3, 'Sân Số 1 (Trảng Dài)', 'Thảm thi đấu quốc tế, độ nảy tiêu chuẩn BWF', 'AVAILABLE'),
(11, 3, 'Sân Số 2 (Trảng Dài)', 'Thảm thi đấu quốc tế, độ nảy tiêu chuẩn BWF', 'AVAILABLE'),
(12, 3, 'Sân Số 3 (Trảng Dài)', 'Thảm thi đấu quốc tế, độ nảy tiêu chuẩn BWF', 'AVAILABLE'),
(13, 4, 'Sân Số 1 (Hố Nai)', 'Không gian thoáng mát, trần cao 9m không lóa', 'AVAILABLE'),
(14, 4, 'Sân Số 2 (Hố Nai)', 'Không gian thoáng mát, trần cao 9m không lóa', 'AVAILABLE');

-- 6. Seed Staff - Branch Assignments
INSERT IGNORE INTO `staff_branches` (`id`, `user_id`, `branch_id`) VALUES
(1, 2, 1), -- Manager quản lý cơ sở Long Khánh
(2, 3, 1); -- Staff làm việc tại cơ sở Long Khánh

-- 7. Seed Pricing Rules for all Courts & Slots dynamically
-- Clean up existing pricing rules to prevent duplicates
DELETE FROM `pricing_rules`;

-- Seed WEEKDAY & WEEKEND pricing for all Courts & Slots
INSERT INTO `pricing_rules` (`court_id`, `slot_id`, `day_type`, `price`, `status`, `created_at`, `updated_at`)
SELECT 
    c.id AS court_id,
    s.id AS slot_id,
    dt.day_type,
    CASE 
        WHEN dt.day_type = 'WEEKEND' THEN 
            CASE 
                WHEN s.id IN (13, 14, 15) THEN 80000.00 -- peak hours
                WHEN s.id = 18 THEN 55000.00            -- late night
                ELSE 65000.00
            END
        ELSE -- WEEKDAY
            CASE 
                WHEN s.id IN (13, 14, 15) THEN 70000.00
                WHEN s.id = 18 THEN 45000.00
                ELSE 50000.00
            END
    END AS price,
    'ACTIVE' AS status,
    CURRENT_TIMESTAMP AS created_at,
    CURRENT_TIMESTAMP AS updated_at
FROM `courts` c
CROSS JOIN `time_slots` s
CROSS JOIN (
    SELECT 'WEEKDAY' AS day_type
    UNION ALL SELECT 'WEEKEND'
) dt;

-- 8. Seed Concession / Rental Products
INSERT IGNORE INTO `products` (`id`, `name`, `product_type`, `unit`, `charge_type`, `price`, `status`) VALUES
(1, 'Nước suối Aquafina 500ml', 'SELL', 'Chai', 'PER_UNIT', 10000.00, 'ACTIVE'),
(2, 'Nước ngọt Revive 500ml', 'SELL', 'Chai', 'PER_UNIT', 15000.00, 'ACTIVE'),
(3, 'Quấn cán vợt Yonex', 'SELL', 'Cái', 'PER_UNIT', 15000.00, 'ACTIVE'),
(4, 'Thuê vợt Yonex Astrox 88D Play', 'RENT', 'Cây', 'PER_SLOT', 30000.00, 'ACTIVE');

-- 9. Seed Inventory for Branches
INSERT IGNORE INTO `branch_inventories` (`id`, `branch_id`, `product_id`, `quantity`, `low_stock_threshold`) VALUES
(1, 1, 1, 100, 10),
(2, 1, 2, 50, 5),
(3, 1, 3, 30, 5),
(4, 1, 4, 10, 2),
(5, 2, 1, 80, 10);

-- 10. Update existing plain-text passwords to BCrypt hashes (since INSERT IGNORE won't update them)
UPDATE `users` SET `password` = '$2a$10$hqzCXOgCexwl.xL0qCB7e.zP4NKUMyXYnhUqyJJdXfHrQRXMcSihG' WHERE `id` IN (1, 2, 3);
UPDATE `users` SET `password` = '$2a$10$q8yBkWgMk/CuuL3vWwYtAOzBb2Mdex4S4Dvk12x4D3Kaica2IrbJa' WHERE `id` = 4;

-- 11. Seed Court Images with local images stored in FE public/courts directory
-- Clear old image records to ensure no duplicates or dirty data exists
DELETE FROM `court_images`;

INSERT IGNORE INTO `court_images` (`id`, `court_id`, `image_url`, `is_primary`, `created_at`) VALUES
-- Sân 1 (Long Khánh)
(1, 1, '/courts/sân-1-1.jpg', TRUE, CURRENT_TIMESTAMP),
(2, 1, '/courts/sân-1-2.jpg', FALSE, CURRENT_TIMESTAMP),

-- Sân 2 (Long Khánh)
(3, 2, '/courts/sân-2-1.jpg', TRUE, CURRENT_TIMESTAMP),
(4, 2, '/courts/sân-2-2.jpg', FALSE, CURRENT_TIMESTAMP),

-- Sân 3 (Long Khánh)
(5, 3, '/courts/sân-1-2.jpg', TRUE, CURRENT_TIMESTAMP),
(6, 3, '/courts/sân-2-1.jpg', FALSE, CURRENT_TIMESTAMP),

-- Sân 4 (Biên Hòa)
(7, 4, '/courts/sân-2-2.jpg', TRUE, CURRENT_TIMESTAMP),
(8, 4, '/courts/sân-1-1.jpg', FALSE, CURRENT_TIMESTAMP),

-- Sân 5 (Biên Hòa)
(9, 5, '/courts/sân-1-1.jpg', TRUE, CURRENT_TIMESTAMP),
(10, 5, '/courts/sân-2-1.jpg', FALSE, CURRENT_TIMESTAMP),

-- Sân 4 (Long Khánh)
(11, 6, '/courts/sân-2-1.jpg', TRUE, CURRENT_TIMESTAMP),
(12, 6, '/courts/sân-1-2.jpg', FALSE, CURRENT_TIMESTAMP),

-- Sân 5 (Long Khánh)
(13, 7, '/courts/sân-2-2.jpg', TRUE, CURRENT_TIMESTAMP),

-- Sân 3 (Biên Hòa)
(14, 8, '/courts/sân-1-1.jpg', TRUE, CURRENT_TIMESTAMP),

-- Sân 4 (Biên Hòa)
(15, 9, '/courts/sân-2-1.jpg', TRUE, CURRENT_TIMESTAMP),

-- Sân 1 (Trảng Dài)
(16, 10, '/courts/sân-1-2.jpg', TRUE, CURRENT_TIMESTAMP),

-- Sân 2 (Trảng Dài)
(17, 11, '/courts/sân-2-2.jpg', TRUE, CURRENT_TIMESTAMP),

-- Sân 3 (Trảng Dài)
(18, 12, '/courts/sân-1-1.jpg', TRUE, CURRENT_TIMESTAMP),

-- Sân 1 (Hố Nai)
(19, 13, '/courts/sân-2-1.jpg', TRUE, CURRENT_TIMESTAMP),

-- Sân 2 (Hố Nai)
(20, 14, '/courts/sân-1-2.jpg', TRUE, CURRENT_TIMESTAMP);

-- 12. Seed Cancellation Policies for Branches
DELETE FROM `cancellation_policies`;
INSERT IGNORE INTO `cancellation_policies` (`id`, `branch_id`, `hours_before`, `refund_percentage`, `status`) VALUES
(1, 1, 24, 100.00, 'ACTIVE'), -- Hủy trước 24 giờ: hoàn 100%
(2, 1, 12, 50.00, 'ACTIVE'),  -- Hủy trước 12 giờ: hoàn 50%
(3, 1, 0, 0.00, 'ACTIVE'),     -- Hủy dưới 12 giờ: hoàn 0%
(4, 2, 24, 100.00, 'ACTIVE'),
(5, 2, 12, 50.00, 'ACTIVE'),
(6, 2, 0, 0.00, 'ACTIVE');

-- 13. Seed Test User 5 (Nguyễn Văn A) if not exists and update password to 'customer123'
INSERT IGNORE INTO `users` (`id`, `email`, `phone_number`, `password`, `full_name`, `role`, `status`) VALUES
(5, 'ngyenvana@gmail.com', '0932455679', '$2a$10$q8yBkWgMk/CuuL3vWwYtAOzBb2Mdex4S4Dvk12x4D3Kaica2IrbJa', 'Nguyễn Văn A', 'CUSTOMER', 'ACTIVE');
UPDATE `users` SET `password` = '$2a$10$q8yBkWgMk/CuuL3vWwYtAOzBb2Mdex4S4Dvk12x4D3Kaica2IrbJa' WHERE `id` = 5;

-- 14. Clean-up old mock test booking data
DELETE FROM `refunds` WHERE `id` IN (101);
DELETE FROM `payments` WHERE `id` IN (101, 102, 103, 104);
DELETE FROM `court_reservations` WHERE `id` IN (101, 102, 103, 104);
DELETE FROM `booking_details` WHERE `id` IN (101, 102, 103, 104);
DELETE FROM `bookings` WHERE `id` IN (101, 102, 103, 104);

-- 15. Seed Mock Bookings for Nguyễn Văn A (from 30/6 to 2/7)
INSERT IGNORE INTO `bookings` (`id`, `booking_code`, `user_id`, `voucher_id`, `discount_amount`, `total_price`, `booking_status`, `payment_status`, `created_at`) VALUES
-- Đơn đã hoàn thành (ngày 30/06)
(101, 'BK-260630-0001', 5, NULL, 0.00, 50000.00, 'COMPLETED', 'PAID', '2026-06-30 10:00:00'),
-- Đơn sắp chơi ngày mai 03/07 (Có thể test hủy đơn hoàn tiền)
(102, 'BK-260702-0001', 5, NULL, 0.00, 50000.00, 'CONFIRMED', 'PAID', '2026-07-02 09:00:00'),
-- Đơn sắp chơi ngày mốt 04/07 (Có thể test hủy đơn hoàn tiền)
(103, 'BK-260702-0002', 5, NULL, 0.00, 50000.00, 'CONFIRMED', 'PAID', '2026-07-02 11:30:00'),
-- Đơn đã hủy và hoàn tiền (ngày 01/07)
(104, 'BK-260701-0001', 5, NULL, 0.00, 50000.00, 'CANCELLED', 'REFUNDED', '2026-07-01 14:00:00');

-- 16. Seed Mock Booking Details
INSERT IGNORE INTO `booking_details` (`id`, `booking_id`, `court_id`, `slot_id`, `booking_date`, `unit_price`, `detail_status`, `created_at`) VALUES
(101, 101, 1, 14, '2026-06-30', 50000.00, 'COMPLETED', '2026-06-30 10:00:00'),
(102, 102, 2, 14, '2026-07-03', 50000.00, 'BOOKED', '2026-07-02 09:00:00'),
(103, 103, 1, 14, '2026-07-04', 50000.00, 'BOOKED', '2026-07-02 11:30:00'),
(104, 104, 1, 14, '2026-07-01', 50000.00, 'CANCELLED', '2026-07-01 14:00:00');

-- 17. Seed Mock Court Reservations
INSERT IGNORE INTO `court_reservations` (`id`, `court_id`, `slot_id`, `reservation_date`, `source_type`, `source_id`, `status`, `is_active`, `note`) VALUES
(101, 1, 14, '2026-06-30', 'BOOKING', 101, 'COMPLETED', 1, 'Lịch giữ chỗ cho hóa đơn: BK-260630-0001'),
(102, 2, 14, '2026-07-03', 'BOOKING', 102, 'ACTIVE', 1, 'Lịch giữ chỗ cho hóa đơn: BK-260702-0001'),
(103, 1, 14, '2026-07-04', 'BOOKING', 103, 'ACTIVE', 1, 'Lịch giữ chỗ cho hóa đơn: BK-260702-0002'),
(104, 1, 14, '2026-07-01', 'BOOKING', 104, 'CANCELLED', NULL, 'Hủy giữ sân đơn đặt: BK-260701-0001');

-- 18. Seed Mock Payments
INSERT IGNORE INTO `payments` (`id`, `booking_id`, `payment_method`, `amount`, `transaction_code`, `gateway_transaction_id`, `payment_status`, `pay_date`) VALUES
(101, 101, 'BANKING', 50000.00, 'TXN-BK-260630-0001', 'MOCK-TXN-1', 'SUCCESS', '2026-06-30 10:05:00'),
(102, 102, 'VNPAY', 50000.00, 'TXN-BK-260702-0001', 'VNP-20260702090500', 'SUCCESS', '2026-07-02 09:05:00'),
(103, 103, 'MOMO', 50000.00, 'TXN-BK-260702-0002', 'MOMO-20260702113500', 'SUCCESS', '2026-07-02 11:35:00'),
(104, 104, 'BANKING', 50000.00, 'TXN-BK-260701-0001', 'MOCK-TXN-4', 'REFUNDED', '2026-07-01 14:05:00');

-- 19. Seed Mock Refund
INSERT IGNORE INTO `refunds` (`id`, `payment_id`, `refund_code`, `refund_amount`, `refund_reason`, `gateway_refund_id`, `status`) VALUES
(101, 104, 'REF-104', 50000.00, 'Khách hàng hủy đơn đặt sân', 'MOCK-REF-4', 'SUCCESS');

-- 20. Seed More Users (tranvanb, lethic, phamvand) for rich data display
INSERT IGNORE INTO `users` (`id`, `email`, `phone_number`, `password`, `full_name`, `role`, `status`) VALUES
(6, 'tranvanb@gmail.com', '0911222333', '$2a$10$q8yBkWgMk/CuuL3vWwYtAOzBb2Mdex4S4Dvk12x4D3Kaica2IrbJa', 'Trần Văn B', 'CUSTOMER', 'ACTIVE'),
(7, 'lethic@gmail.com', '0922333444', '$2a$10$q8yBkWgMk/CuuL3vWwYtAOzBb2Mdex4S4Dvk12x4D3Kaica2IrbJa', 'Lê Thị C', 'CUSTOMER', 'ACTIVE'),
(8, 'phamvand@gmail.com', '0933444555', '$2a$10$q8yBkWgMk/CuuL3vWwYtAOzBb2Mdex4S4Dvk12x4D3Kaica2IrbJa', 'Phạm Văn D', 'CUSTOMER', 'ACTIVE');

-- 21. Clean-up old rich mock bookings
DELETE FROM `refunds` WHERE `id` IN (110);
DELETE FROM `payments` WHERE `id` BETWEEN 110 AND 125;
DELETE FROM `court_reservations` WHERE `id` BETWEEN 110 AND 125;
DELETE FROM `booking_details` WHERE `id` BETWEEN 110 AND 125;
DELETE FROM `bookings` WHERE `id` BETWEEN 110 AND 125;
DELETE FROM `subscription_schedules` WHERE `id` BETWEEN 110 AND 115;
DELETE FROM `subscriptions` WHERE `id` BETWEEN 110 AND 115;

-- 22. Seed rich bookings dataset
INSERT IGNORE INTO `bookings` (`id`, `booking_code`, `user_id`, `voucher_id`, `discount_amount`, `total_price`, `booking_status`, `payment_status`, `created_at`) VALUES
(110, 'BK-260626-0001', 6, NULL, 0.00, 50000.00, 'COMPLETED', 'PAID', '2026-06-26 18:00:00'),
(111, 'BK-260627-0001', 7, NULL, 0.00, 50000.00, 'COMPLETED', 'PAID', '2026-06-27 17:00:00'),
(112, 'BK-260628-0001', 8, NULL, 0.00, 50000.00, 'COMPLETED', 'PAID', '2026-06-28 19:30:00'),
(113, 'BK-260629-0001', 6, NULL, 0.00, 100000.00, 'COMPLETED', 'PAID', '2026-06-29 18:15:00'),
(114, 'BK-260630-0002', 7, NULL, 0.00, 50000.00, 'COMPLETED', 'PAID', '2026-06-30 20:00:00'),
(115, 'BK-260701-0002', 8, NULL, 0.00, 50000.00, 'COMPLETED', 'PAID', '2026-07-01 17:30:00'),
(116, 'BK-260701-0003', 6, NULL, 0.00, 50000.00, 'CANCELLED', 'REFUNDED', '2026-07-01 19:00:00'),
(117, 'BK-260702-0003', 7, NULL, 0.00, 100000.00, 'CONFIRMED', 'PAID', '2026-07-02 10:00:00'),
(118, 'BK-260702-0004', 8, NULL, 0.00, 50000.00, 'CONFIRMED', 'PAID', '2026-07-02 14:00:00'),
(119, 'BK-260702-0005', 6, NULL, 0.00, 50000.00, 'CANCELLED', 'UNPAID', '2026-07-02 16:00:00');

-- 23. Seed rich booking details dataset
INSERT IGNORE INTO `booking_details` (`id`, `booking_id`, `court_id`, `slot_id`, `booking_date`, `unit_price`, `detail_status`, `created_at`) VALUES
(110, 110, 1, 13, '2026-06-26', 50000.00, 'COMPLETED', '2026-06-26 18:00:00'),
(111, 111, 2, 14, '2026-06-27', 50000.00, 'COMPLETED', '2026-06-27 17:00:00'),
(112, 112, 3, 15, '2026-06-28', 50000.00, 'COMPLETED', '2026-06-28 19:30:00'),
(113, 113, 1, 14, '2026-06-29', 50000.00, 'COMPLETED', '2026-06-29 18:15:00'),
(114, 113, 4, 15, '2026-06-29', 50000.00, 'COMPLETED', '2026-06-29 18:15:00'),
(115, 114, 2, 14, '2026-06-30', 50000.00, 'COMPLETED', '2026-06-30 20:00:00'),
(116, 115, 5, 15, '2026-07-01', 50000.00, 'COMPLETED', '2026-07-01 17:30:00'),
(117, 116, 3, 14, '2026-07-01', 50000.00, 'CANCELLED', '2026-07-01 19:00:00'),
(118, 117, 1, 14, '2026-07-03', 50000.00, 'BOOKED', '2026-07-02 10:00:00'),
(119, 117, 2, 15, '2026-07-03', 50000.00, 'BOOKED', '2026-07-02 10:00:00'),
(120, 118, 4, 14, '2026-07-04', 50000.00, 'BOOKED', '2026-07-02 14:00:00'),
(121, 119, 1, 13, '2026-07-02', 50000.00, 'CANCELLED', '2026-07-02 16:00:00');

-- 24. Seed rich court reservations dataset
INSERT IGNORE INTO `court_reservations` (`id`, `court_id`, `slot_id`, `reservation_date`, `source_type`, `source_id`, `status`, `is_active`, `note`) VALUES
(110, 1, 13, '2026-06-26', 'BOOKING', 110, 'COMPLETED', 1, 'Lịch giữ chỗ'),
(111, 2, 14, '2026-06-27', 'BOOKING', 111, 'COMPLETED', 1, 'Lịch giữ chỗ'),
(112, 3, 15, '2026-06-28', 'BOOKING', 112, 'COMPLETED', 1, 'Lịch giữ chỗ'),
(113, 1, 14, '2026-06-29', 'BOOKING', 113, 'COMPLETED', 1, 'Lịch giữ chỗ'),
(114, 4, 15, '2026-06-29', 'BOOKING', 114, 'COMPLETED', 1, 'Lịch giữ chỗ'),
(115, 2, 14, '2026-06-30', 'BOOKING', 115, 'COMPLETED', 1, 'Lịch giữ chỗ'),
(116, 5, 15, '2026-07-01', 'BOOKING', 116, 'COMPLETED', 1, 'Lịch giữ chỗ'),
(117, 3, 14, '2026-07-01', 'BOOKING', 117, 'CANCELLED', NULL, 'Hủy giữ sân'),
(118, 1, 14, '2026-07-03', 'BOOKING', 118, 'ACTIVE', 1, 'Lịch giữ chỗ'),
(119, 2, 15, '2026-07-03', 'BOOKING', 119, 'ACTIVE', 1, 'Lịch giữ chỗ'),
(120, 4, 14, '2026-07-04', 'BOOKING', 120, 'ACTIVE', 1, 'Lịch giữ chỗ'),
(121, 1, 13, '2026-07-02', 'BOOKING', 121, 'CANCELLED', NULL, 'Hủy giữ sân');

-- 25. Seed rich payments dataset
INSERT IGNORE INTO `payments` (`id`, `booking_id`, `payment_method`, `amount`, `transaction_code`, `gateway_transaction_id`, `payment_status`, `pay_date`) VALUES
(110, 110, 'CASH', 50000.00, 'TXN-BK-260626-0001', 'MOCK-TXN-110', 'SUCCESS', '2026-06-26 18:05:00'),
(111, 111, 'BANKING', 50000.00, 'TXN-BK-260627-0001', 'MOCK-TXN-111', 'SUCCESS', '2026-06-27 17:05:00'),
(112, 112, 'VNPAY', 50000.00, 'TXN-BK-260628-0001', 'VNP-20260628193500', 'SUCCESS', '2026-06-28 19:35:00'),
(113, 113, 'MOMO', 100000.00, 'TXN-BK-260629-0001', 'MOMO-20260629182000', 'SUCCESS', '2026-06-29 18:20:00'),
(114, 114, 'BANKING', 50000.00, 'TXN-BK-260630-0002', 'MOCK-TXN-114', 'SUCCESS', '2026-06-30 20:05:00'),
(115, 115, 'VNPAY', 50000.00, 'TXN-BK-260701-0002', 'VNP-20260701173500', 'SUCCESS', '2026-07-01 17:35:00'),
(116, 116, 'MOMO', 50000.00, 'TXN-BK-260701-0003', 'MOMO-20260701190500', 'REFUNDED', '2026-07-01 19:05:00'),
(117, 117, 'BANKING', 100000.00, 'TXN-BK-260702-0003', 'MOCK-TXN-117', 'SUCCESS', '2026-07-02 10:05:00'),
(118, 118, 'VNPAY', 50000.00, 'TXN-BK-260702-0004', 'VNP-20260702140500', 'SUCCESS', '2026-07-02 14:05:00');

-- 26. Seed rich refunds dataset
INSERT IGNORE INTO `refunds` (`id`, `payment_id`, `refund_code`, `refund_amount`, `refund_reason`, `gateway_refund_id`, `status`) VALUES
(110, 116, 'REF-116', 50000.00, 'Khách hàng hủy đặt sân', 'MOCK-REF-116', 'SUCCESS');

-- 27. Seed rich subscriptions dataset
INSERT IGNORE INTO `subscriptions` (`id`, `subscription_code`, `user_id`, `branch_id`, `start_date`, `end_date`, `total_price`, `status`, `created_at`) VALUES
(110, 'SUB-260620-0001', 6, 1, '2026-06-20', '2026-09-20', 650000.00, 'ACTIVE', '2026-06-20 09:00:00'),
(111, 'SUB-260625-0001', 7, 2, '2026-06-25', '2026-09-25', 700000.00, 'ACTIVE', '2026-06-25 10:00:00'),
(112, 'SUB-260630-0001', 8, 1, '2026-06-30', '2026-09-30', 650000.00, 'ACTIVE', '2026-06-30 11:00:00');

-- 28. Seed rich subscription schedules dataset
INSERT IGNORE INTO `subscription_schedules` (`id`, `subscription_id`, `court_id`, `slot_id`, `day_of_week`, `status`, `created_at`) VALUES
(110, 110, 1, 14, 2, 'ACTIVE', '2026-06-20 09:00:00'),
(111, 111, 4, 15, 4, 'ACTIVE', '2026-06-25 10:00:00'),
(112, 112, 2, 14, 6, 'ACTIVE', '2026-06-30 11:00:00');

-- 29. Seed Vouchers
INSERT IGNORE INTO `vouchers` (`id`, `code`, `discount_type`, `discount_value`, `min_order_value`, `max_discount`, `usage_limit`, `used_count`, `start_date`, `end_date`, `status`) VALUES
(1, 'CHAOMUNG2026', 'PERCENT', 10.00, 50000.00, 30000.00, 500, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE'),
(2, 'KM50K', 'AMOUNT', 50000.00, 150000.00, NULL, 200, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE'),
(3, 'SANTOI', 'PERCENT', 20.00, 100000.00, 50000.00, 100, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE'),
(4, 'VIPMEMBER', 'PERCENT', 15.00, 200000.00, 100000.00, 50, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE');
