-- ==========================================
-- BACKEND SKELETON DATABASE CREATION SCRIPT
-- Project: Website quản lý và đặt sân cầu lông
-- Platform: phpMyAdmin / MySQL / MariaDB
-- ==========================================

-- Hướng dẫn sử dụng:
-- 1. Mở phpMyAdmin.
-- 2. Nhấn vào mục "SQL" hoặc tạo một database mới tên là `badminton_db` rồi vào mục "Import".
-- 3. Chọn file này để chạy tạo toàn bộ cấu trúc bảng và các ràng buộc khóa ngoại.

CREATE DATABASE IF NOT EXISTS `badminton_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `badminton_db`;

-- Tắt kiểm tra khóa ngoại tạm thời để tránh lỗi xung đột thứ tự tạo bảng
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------
-- 1. Bảng branches (Chi nhánh/Cơ sở)
-- ------------------------------------------
DROP TABLE IF EXISTS `branches`;
CREATE TABLE `branches` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT 'Tên chi nhánh',
  `address` VARCHAR(255) NOT NULL COMMENT 'Địa chỉ vật lý',
  `phone_number` VARCHAR(15) DEFAULT NULL COMMENT 'Số hotline chi nhánh',
  `open_time` TIME NOT NULL COMMENT 'Giờ mở cửa',
  `close_time` TIME NOT NULL COMMENT 'Giờ đóng cửa',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái hoạt động',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_branches_status` CHECK (`status` IN ('ACTIVE', 'MAINTENANCE', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 2. Bảng time_slots (Ca thi đấu)
-- ------------------------------------------
DROP TABLE IF EXISTS `time_slots`;
CREATE TABLE `time_slots` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `start_time` TIME NOT NULL COMMENT 'Giờ bắt đầu ca',
  `end_time` TIME NOT NULL COMMENT 'Giờ kết thúc ca',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_time_slots_time` (`start_time`, `end_time`),
  CONSTRAINT `chk_time_slots_status` CHECK (`status` IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 3. Bảng system_configs (Cấu hình hệ thống)
-- ------------------------------------------
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL COMMENT 'Từ khóa cấu hình',
  `config_value` VARCHAR(255) NOT NULL COMMENT 'Giá trị cấu hình',
  `description` VARCHAR(255) DEFAULT NULL COMMENT 'Mô tả tham số',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 4. Bảng users (Tài khoản người dùng)
-- ------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(100) NOT NULL COMMENT 'Email đăng nhập',
  `phone_number` VARCHAR(15) NOT NULL COMMENT 'Số điện thoại',
  `password` VARCHAR(255) NOT NULL COMMENT 'Mật khẩu mã hóa BCrypt',
  `full_name` VARCHAR(50) NOT NULL COMMENT 'Họ và tên',
  `role` VARCHAR(20) NOT NULL COMMENT 'Phân quyền',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái tài khoản',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_phone` (`phone_number`),
  CONSTRAINT `chk_users_role` CHECK (`role` IN ('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')),
  CONSTRAINT `chk_users_status` CHECK (`status` IN ('ACTIVE', 'LOCKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 5. Bảng vouchers (Mã giảm giá)
-- ------------------------------------------
DROP TABLE IF EXISTS `vouchers`;
CREATE TABLE `vouchers` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL COMMENT 'Mã coupon nhập ở FE',
  `discount_type` VARCHAR(20) NOT NULL COMMENT 'Hình thức giảm PERCENT / AMOUNT',
  `discount_value` DECIMAL(12,2) NOT NULL COMMENT 'Giá trị giảm',
  `min_order_value` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT 'Đơn tối thiểu áp dụng',
  `max_discount` DECIMAL(12,2) DEFAULT NULL COMMENT 'Giảm tối đa (áp dụng cho PERCENT)',
  `usage_limit` INT NOT NULL COMMENT 'Tổng lượt dùng tối đa',
  `used_count` INT NOT NULL DEFAULT '0' COMMENT 'Lượt dùng thực tế',
  `start_date` TIMESTAMP NOT NULL COMMENT 'Ngày bắt đầu áp dụng',
  `end_date` TIMESTAMP NOT NULL COMMENT 'Ngày hết hạn',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái coupon',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vouchers_code` (`code`),
  CONSTRAINT `chk_vouchers_type` CHECK (`discount_type` IN ('PERCENT', 'AMOUNT')),
  CONSTRAINT `chk_vouchers_limit` CHECK (`usage_limit` > 0),
  CONSTRAINT `chk_vouchers_used` CHECK (`used_count` >= 0),
  CONSTRAINT `chk_vouchers_status` CHECK (`status` IN ('ACTIVE', 'EXPIRED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 6. Bảng products (Sản phẩm bán/thuê)
-- ------------------------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT 'Tên mặt hàng/dịch vụ',
  `product_type` VARCHAR(20) NOT NULL COMMENT 'Bán SELL / Thuê RENT',
  `unit` VARCHAR(20) NOT NULL COMMENT 'Đơn vị tính',
  `charge_type` VARCHAR(20) NOT NULL COMMENT 'Theo số lượng PER_UNIT / Theo ca chơi PER_SLOT',
  `price` DECIMAL(12,2) NOT NULL COMMENT 'Đơn giá',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_products_type` CHECK (`product_type` IN ('SELL', 'RENT')),
  CONSTRAINT `chk_products_charge` CHECK (`charge_type` IN ('PER_UNIT', 'PER_SLOT')),
  CONSTRAINT `chk_products_status` CHECK (`status` IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 7. Bảng courts (Danh mục sân cầu lông nhỏ)
-- ------------------------------------------
DROP TABLE IF EXISTS `courts`;
CREATE TABLE `courts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `branch_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL COMMENT 'Tên sân (Sân số 1...)',
  `description` TEXT DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_courts_branch` (`branch_id`),
  CONSTRAINT `fk_courts_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_courts_status` CHECK (`status` IN ('AVAILABLE', 'MAINTENANCE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 8. Bảng staff_branches (Phân công nhân viên)
-- ------------------------------------------
DROP TABLE IF EXISTS `staff_branches`;
CREATE TABLE `staff_branches` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `branch_id` BIGINT NOT NULL,
  `assigned_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_branches` (`user_id`, `branch_id`),
  KEY `fk_staff_branch_facility` (`branch_id`),
  CONSTRAINT `fk_staff_branch_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_staff_branch_facility` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 9. Bảng cancellation_policies (Chính sách hủy sân)
-- ------------------------------------------
DROP TABLE IF EXISTS `cancellation_policies`;
CREATE TABLE `cancellation_policies` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `branch_id` BIGINT NOT NULL,
  `hours_before` INT NOT NULL COMMENT 'Thời gian hủy trước lúc chơi (giờ)',
  `refund_percentage` DECIMAL(5,2) NOT NULL COMMENT 'Tỷ lệ hoàn tiền (%)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  KEY `fk_cancel_policy_branch` (`branch_id`),
  CONSTRAINT `fk_cancel_policy_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_cancel_hours` CHECK (`hours_before` >= 0),
  CONSTRAINT `chk_cancel_refund` CHECK (`refund_percentage` BETWEEN 0.00 AND 100.00),
  CONSTRAINT `chk_cancel_status` CHECK (`status` IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 10. Bảng court_images (Hình ảnh sân)
-- ------------------------------------------
DROP TABLE IF EXISTS `court_images`;
CREATE TABLE `court_images` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `court_id` BIGINT NOT NULL,
  `image_url` VARCHAR(255) NOT NULL,
  `is_primary` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Đánh dấu ảnh đại diện chính',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_court_images_court` (`court_id`),
  CONSTRAINT `fk_court_images_court` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 11. Bảng pricing_rules (Bảng giá cấu hình)
-- ------------------------------------------
DROP TABLE IF EXISTS `pricing_rules`;
CREATE TABLE `pricing_rules` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `court_id` BIGINT NOT NULL,
  `slot_id` BIGINT NOT NULL,
  `day_type` VARCHAR(20) NOT NULL COMMENT 'WEEKDAY / WEEKEND',
  `price` DECIMAL(12,2) NOT NULL COMMENT 'Đơn giá ca',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pricing_rules` (`court_id`, `slot_id`, `day_type`),
  KEY `fk_pricing_slot` (`slot_id`),
  CONSTRAINT `fk_pricing_court` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pricing_slot` FOREIGN KEY (`slot_id`) REFERENCES `time_slots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_pricing_day_type` CHECK (`day_type` IN ('WEEKDAY', 'WEEKEND')),
  CONSTRAINT `chk_pricing_status` CHECK (`status` IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 12. Bảng branch_inventories (Số lượng tồn kho chi nhánh)
-- ------------------------------------------
DROP TABLE IF EXISTS `branch_inventories`;
CREATE TABLE `branch_inventories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `branch_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT '0',
  `low_stock_threshold` INT DEFAULT '5',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_branch_product_inventory` (`branch_id`, `product_id`),
  KEY `fk_inventory_product` (`product_id`),
  CONSTRAINT `fk_inventory_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_inventory_qty` CHECK (`quantity` >= 0),
  CONSTRAINT `chk_inventory_threshold` CHECK (`low_stock_threshold` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 13. Bảng bookings (Hóa đơn đặt sân)
-- ------------------------------------------
DROP TABLE IF EXISTS `bookings`;
CREATE TABLE `bookings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_code` VARCHAR(50) NOT NULL COMMENT 'Mã tự sinh định danh BK...',
  `user_id` BIGINT NOT NULL,
  `voucher_id` BIGINT DEFAULT NULL,
  `discount_amount` DECIMAL(12,2) DEFAULT '0.00',
  `total_price` DECIMAL(12,2) NOT NULL COMMENT 'Tổng thanh toán cuối sau khi giảm giá',
  `booking_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Vòng đời đơn',
  `payment_status` VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT 'Trạng thái thanh toán',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bookings_code` (`booking_code`),
  KEY `fk_bookings_user` (`user_id`),
  KEY `fk_bookings_voucher` (`voucher_id`),
  CONSTRAINT `fk_bookings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_bookings_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chk_bookings_status` CHECK (`booking_status` IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
  CONSTRAINT `chk_bookings_pay_status` CHECK (`payment_status` IN ('UNPAID', 'PARTIAL', 'PAID', 'REFUNDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 14. Bảng booking_details (Chi tiết lịch sân)
-- ------------------------------------------
DROP TABLE IF EXISTS `booking_details`;
CREATE TABLE `booking_details` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `court_id` BIGINT NOT NULL,
  `slot_id` BIGINT NOT NULL,
  `booking_date` DATE NOT NULL,
  `unit_price` DECIMAL(12,2) NOT NULL COMMENT 'Đơn giá lẻ chốt',
  `detail_status` VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_booking_details_booking` (`booking_id`),
  KEY `fk_booking_details_court` (`court_id`),
  KEY `fk_booking_details_slot` (`slot_id`),
  CONSTRAINT `fk_booking_details_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_booking_details_court` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_booking_details_slot` FOREIGN KEY (`slot_id`) REFERENCES `time_slots` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_booking_details_status` CHECK (`detail_status` IN ('BOOKED', 'CANCELLED', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 15. Bảng court_reservations (Giữ lịch, chống trùng & khóa sân)
-- ------------------------------------------
DROP TABLE IF EXISTS `court_reservations`;
CREATE TABLE `court_reservations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `court_id` BIGINT NOT NULL,
  `slot_id` BIGINT NOT NULL,
  `reservation_date` DATE NOT NULL,
  `source_type` VARCHAR(20) NOT NULL COMMENT 'BOOKING / SUBSCRIPTION / MAINTENANCE / ADMIN_BLOCK',
  `source_id` BIGINT DEFAULT NULL COMMENT 'ID liên kết booking_details hoặc subscription_schedules',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `is_active` TINYINT(1) DEFAULT '1' COMMENT 'Cờ check, khi hủy set NULL để nhả slot',
  `note` VARCHAR(255) DEFAULT NULL COMMENT 'Lý do khóa sân',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_court_reservations_slot` (`court_id`, `slot_id`, `reservation_date`, `is_active`),
  KEY `fk_reservations_slot` (`slot_id`),
  CONSTRAINT `fk_reservations_court` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reservations_slot` FOREIGN KEY (`slot_id`) REFERENCES `time_slots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_reservations_src_type` CHECK (`source_type` IN ('BOOKING', 'SUBSCRIPTION', 'MAINTENANCE', 'ADMIN_BLOCK')),
  CONSTRAINT `chk_reservations_status` CHECK (`status` IN ('ACTIVE', 'CANCELLED', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 16. Bảng payments (Giao dịch thanh toán)
-- ------------------------------------------
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `payment_method` VARCHAR(20) NOT NULL COMMENT 'VNPAY / MOMO / BANKING / CASH',
  `amount` DECIMAL(12,2) NOT NULL,
  `transaction_code` VARCHAR(100) NOT NULL COMMENT 'Mã hệ thống tự sinh đối soát',
  `gateway_transaction_id` VARCHAR(100) DEFAULT NULL COMMENT 'Mã phản hồi từ cổng đối tác',
  `payment_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `raw_response` TEXT DEFAULT NULL COMMENT 'JSON phản hồi từ cổng thanh toán',
  `pay_date` DATETIME DEFAULT NULL COMMENT 'Mốc giờ giao dịch thành công',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payments_txn` (`transaction_code`),
  KEY `fk_payments_booking` (`booking_id`),
  CONSTRAINT `fk_payments_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_payments_method` CHECK (`payment_method` IN ('VNPAY', 'MOMO', 'BANKING', 'CASH')),
  CONSTRAINT `chk_payments_status` CHECK (`payment_status` IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 17. Bảng booking_services (Dịch vụ đi kèm đơn đặt)
-- ------------------------------------------
DROP TABLE IF EXISTS `booking_services`;
CREATE TABLE `booking_services` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `unit_price` DECIMAL(12,2) NOT NULL,
  `total_price` DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_booking_services_booking` (`booking_id`),
  KEY `fk_booking_services_product` (`product_id`),
  CONSTRAINT `fk_booking_services_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_booking_services_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_booking_service_qty` CHECK (`quantity` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 18. Bảng voucher_usages (Lịch sử áp dụng voucher)
-- ------------------------------------------
DROP TABLE IF EXISTS `voucher_usages`;
CREATE TABLE `voucher_usages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `voucher_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `booking_id` BIGINT NOT NULL,
  `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT '0.00',
  `used_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_booking_usage` (`voucher_id`, `booking_id`),
  KEY `fk_voucher_usage_user` (`user_id`),
  KEY `fk_voucher_usage_booking` (`booking_id`),
  CONSTRAINT `fk_voucher_usage_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_voucher_usage_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_voucher_usage_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 19. Bảng refunds (Lịch sử hoàn trả tiền)
-- ------------------------------------------
DROP TABLE IF EXISTS `refunds`;
CREATE TABLE `refunds` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_id` BIGINT NOT NULL,
  `refund_code` VARCHAR(50) NOT NULL COMMENT 'Mã hoàn tiền nội bộ',
  `refund_amount` DECIMAL(12,2) NOT NULL,
  `refund_reason` VARCHAR(255) DEFAULT NULL,
  `gateway_refund_id` VARCHAR(100) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_code` (`refund_code`),
  KEY `fk_refunds_payment` (`payment_id`),
  CONSTRAINT `fk_refunds_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_refunds_status` CHECK (`status` IN ('PENDING', 'SUCCESS', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 20. Bảng reviews (Đánh giá)
-- ------------------------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `court_id` BIGINT NOT NULL,
  `booking_detail_id` BIGINT NOT NULL COMMENT 'Chỉ đặt chơi xong mới được đánh giá',
  `rating` INT NOT NULL,
  `comment` TEXT DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_booking_detail` (`booking_detail_id`),
  KEY `fk_reviews_user` (`user_id`),
  KEY `fk_reviews_court` (`court_id`),
  CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reviews_court` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reviews_detail` FOREIGN KEY (`booking_detail_id`) REFERENCES `booking_details` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_reviews_rating` CHECK (`rating` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 21. Bảng subscriptions (Lịch cố định định kỳ)
-- ------------------------------------------
DROP TABLE IF EXISTS `subscriptions`;
CREATE TABLE `subscriptions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `subscription_code` VARCHAR(50) NOT NULL COMMENT 'Mã hợp đồng cố định SUB...',
  `user_id` BIGINT NOT NULL,
  `branch_id` BIGINT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `total_price` DECIMAL(12,2) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subscriptions_code` (`subscription_code`),
  KEY `fk_subscriptions_user` (`user_id`),
  KEY `fk_subscriptions_branch` (`branch_id`),
  CONSTRAINT `fk_subscriptions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_subscriptions_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_subscriptions_status` CHECK (`status` IN ('PENDING', 'ACTIVE', 'EXPIRED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------
-- 22. Bảng subscription_schedules (Ca chơi cố định hàng tuần)
-- ------------------------------------------
DROP TABLE IF EXISTS `subscription_schedules`;
CREATE TABLE `subscription_schedules` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `subscription_id` BIGINT NOT NULL,
  `court_id` BIGINT NOT NULL,
  `slot_id` BIGINT NOT NULL,
  `day_of_week` INT NOT NULL COMMENT 'Thứ trong tuần (2 -> 8)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_schedule` (`subscription_id`, `court_id`, `slot_id`, `day_of_week`),
  KEY `fk_sub_schedule_court` (`court_id`),
  KEY `fk_sub_schedule_slot` (`slot_id`),
  CONSTRAINT `fk_sub_schedule_subscription` FOREIGN KEY (`subscription_id`) REFERENCES `subscriptions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sub_schedule_court` FOREIGN KEY (`court_id`) REFERENCES `courts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sub_schedule_slot` FOREIGN KEY (`slot_id`) REFERENCES `time_slots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_sub_schedule_day` CHECK (`day_of_week` BETWEEN 2 AND 8),
  CONSTRAINT `chk_sub_schedule_status` CHECK (`status` IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bật lại kiểm tra khóa ngoại sau khi hoàn thành tạo bảng
SET FOREIGN_KEY_CHECKS = 1;
