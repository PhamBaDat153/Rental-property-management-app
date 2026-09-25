DROP DATABASE IF EXISTS `rental_manage_db`;

CREATE DATABASE IF NOT EXISTS `rental_manage_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `rental_manage_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. ROLES
-- ============================================================

CREATE TABLE IF NOT EXISTS `role` (
    `role_id` BINARY(16) NOT NULL,
    `role_name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(500) NULL,
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',

    PRIMARY KEY (`role_id`),
    UNIQUE KEY `uq_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 2. TENANTS
-- ============================================================

CREATE TABLE IF NOT EXISTS `tenant` (
    `tenant_id` BINARY(16) NOT NULL,
    `full_name` VARCHAR(255) NULL,
    `date_of_birth` DATE NULL,
    `phone` VARCHAR(20) NULL,
    `email` VARCHAR(255) NULL,
    `gender` ENUM('MALE', 'FEMALE', 'OTHER') NULL,
    `avatar_url` TEXT NULL,

    `identity_type` ENUM('CCCD', 'PASSPORT', 'OTHER') NULL,
    `identity_number` VARCHAR(30) NULL,
    `identity_issued_date` DATE NULL,
    `identity_issued_place` VARCHAR(255) NULL,

    `permanent_address` TEXT NULL,
    `emergency_contact_name` VARCHAR(255) NULL,
    `emergency_contact_phone` VARCHAR(20) NULL,

    `additional_note` TEXT NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`tenant_id`),
    UNIQUE KEY `uq_tenant_identity` (`identity_type`, `identity_number`),
    KEY `idx_tenant_phone` (`phone`),
    KEY `idx_tenant_full_name` (`full_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `tenant` MODIFY COLUMN `full_name` VARCHAR(255) NULL;

-- ============================================================
-- 3. USERS
-- tenant_id is nullable because non-tenant users (e.g. LANDLORD)
-- may not have a tenant profile.
-- ============================================================

CREATE TABLE IF NOT EXISTS `user` (
    `user_id` BINARY(16) NOT NULL,
    `role_id` BINARY(16) NOT NULL,
    `tenant_id` BINARY(16) NULL,
    `user_name` VARCHAR(100) NOT NULL,
    `password_hash` VARCHAR(255) NULL,
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uq_user_name` (`user_name`),
    UNIQUE KEY `uq_user_tenant_id` (`tenant_id`),
    KEY `idx_user_role_id` (`role_id`),
    KEY `idx_user_status` (`status`),

    CONSTRAINT `fk_user_role`
        FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `fk_user_tenant`
        FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 4. LOCATION / ROOM
-- ============================================================

CREATE TABLE IF NOT EXISTS `location` (
    `location_id` BINARY(16) NOT NULL,
    `location_code` VARCHAR(50) NOT NULL,
    `address_line` VARCHAR(255) NOT NULL,
    `ward_name` VARCHAR(100) NULL,
    `district_name` VARCHAR(100) NULL,
    `province_name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`location_id`),
    UNIQUE KEY `uq_location_code` (`location_code`),
    KEY `idx_location_address`
        (`province_name`, `district_name`, `ward_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `room` (
    `room_id` BINARY(16) NOT NULL,
    `location_id` BINARY(16) NOT NULL,
    `room_code` VARCHAR(50) NOT NULL,
    `room_name` VARCHAR(100) NULL,
    `floor` INT NULL,
    `area_m2` DECIMAL(10,2) NULL,
    `max_occupants` INT NOT NULL DEFAULT 1,
    `rent_price` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND/month',
    `status` ENUM('AVAILABLE', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    `description` TEXT NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`room_id`),
    UNIQUE KEY `uq_room_location_code` (`location_id`, `room_code`),
    KEY `idx_room_location_id` (`location_id`),
    KEY `idx_room_status` (`status`),

    CONSTRAINT `fk_room_location`
        FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `ck_room_area`
        CHECK (`area_m2` IS NULL OR `area_m2` >= 0),

    CONSTRAINT `ck_room_max_occupants`
        CHECK (`max_occupants` > 0),

    CONSTRAINT `ck_room_rent_price`
        CHECK (`rent_price` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `room_image` (
    `image_id` BINARY(16) NOT NULL,
    `room_id` BINARY(16) NOT NULL,
    `image_url` TEXT NOT NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`image_id`),
    KEY `idx_room_image_room_id` (`room_id`),

    CONSTRAINT `fk_room_image_room`
        FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 5. RENTAL CONTRACTS
-- ============================================================

CREATE TABLE IF NOT EXISTS `rental_contracts` (
    `contract_id` BINARY(16) NOT NULL,
    `room_id` BINARY(16) NOT NULL,

    `start_date` DATE NOT NULL,
    `end_date` DATE NULL,
    `signed_at` DATETIME(3) NULL,

    `rent_amount` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND/month',
    `deposit_required` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND',

    `billing_day` TINYINT UNSIGNED NULL,
    `payment_due_days` SMALLINT UNSIGNED NOT NULL DEFAULT 0,

    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `terms` TEXT NULL,
    `document_url` TEXT NULL,

    `terminated_at` DATETIME(3) NULL,
    `termination_reason` VARCHAR(500) NULL,

    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`contract_id`),
    KEY `idx_contract_room_id` (`room_id`),
    KEY `idx_contract_status` (`status`),
    KEY `idx_contract_dates` (`start_date`, `end_date`),

    CONSTRAINT `fk_contract_room`
        FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `ck_contract_dates`
        CHECK (`end_date` IS NULL OR `end_date` >= `start_date`),

    CONSTRAINT `ck_contract_rent`
        CHECK (`rent_amount` >= 0),

    CONSTRAINT `ck_contract_deposit`
        CHECK (`deposit_required` >= 0),

    CONSTRAINT `ck_contract_billing_day`
        CHECK (`billing_day` IS NULL OR `billing_day` BETWEEN 1 AND 31)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `contract_tenants` (
    `contract_id` BINARY(16) NOT NULL,
    `tenant_id` BINARY(16) NOT NULL,
    `is_representative` BOOLEAN NOT NULL DEFAULT FALSE,
    `move_in_date` DATE NULL,
    `move_out_date` DATE NULL,
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`contract_id`, `tenant_id`),
    KEY `idx_contract_tenants_tenant_id` (`tenant_id`),
    KEY `idx_contract_tenants_status` (`status`),

    CONSTRAINT `fk_contract_tenant_contract`
        FOREIGN KEY (`contract_id`) REFERENCES `rental_contracts` (`contract_id`)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `fk_contract_tenant_tenant`
        FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `ck_contract_tenant_dates`
        CHECK (
            `move_out_date` IS NULL
            OR `move_in_date` IS NULL
            OR `move_out_date` >= `move_in_date`
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 6. ANNOUNCEMENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS `announcement` (
    `announcement_id` BINARY(16) NOT NULL,
    `content` TEXT NOT NULL,
    `announcement_type` VARCHAR(50) NULL,
    `status` ENUM('SENTED', 'NOT_SEND') NOT NULL DEFAULT 'SENTED',
    `sent_at` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`announcement_id`),
    KEY `idx_announcement_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_announcement` (
    `announcement_id` BINARY(16) NOT NULL,
    `user_id` BINARY(16) NOT NULL,
    `status` ENUM('READ', 'NOT_READ') NOT NULL DEFAULT 'NOT_READ',

    PRIMARY KEY (`announcement_id`, `user_id`),

    CONSTRAINT `fk_user_announcement_announcement`
        FOREIGN KEY (`announcement_id`) REFERENCES `announcement` (`announcement_id`)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `fk_user_announcement_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 7. SERVICES
-- ============================================================

CREATE TABLE IF NOT EXISTS `service` (
    `service_id` BINARY(16) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `unit` VARCHAR(50) NOT NULL,
    `calculation_method` VARCHAR(50) NOT NULL,
    `default_unit_price` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND/unit',
    `room_status` ENUM('AVAILABLE', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`service_id`),
    UNIQUE KEY `uq_service_name` (`name`),

    CONSTRAINT `ck_service_price`
        CHECK (`default_unit_price` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `room_service` (
    `room_id` BINARY(16) NOT NULL,
    `service_id` BINARY(16) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`room_id`, `service_id`),
    KEY `idx_room_service_service_id` (`service_id`),

    CONSTRAINT `fk_room_service_room`
        FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `fk_room_service_service`
        FOREIGN KEY (`service_id`) REFERENCES `service` (`service_id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 8. METERS / METER READINGS
-- ============================================================

CREATE TABLE IF NOT EXISTS `meter` (
    `meter_id` BINARY(16) NOT NULL,
    `room_id` BINARY(16) NOT NULL,
    `meter_type` VARCHAR(50) NOT NULL COMMENT 'ELECTRICITY / WATER',
    `status` ENUM('AVAILABLE', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`meter_id`),
    KEY `idx_meter_room_id` (`room_id`),
    KEY `idx_meter_room_type` (`room_id`, `meter_type`),

    CONSTRAINT `fk_meter_room`
        FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `meter_reading` (
    `reading_id` BINARY(16) NOT NULL,
    `meter_id` BINARY(16) NOT NULL,
    `reading_at` DATETIME(3) NOT NULL,
    `current_value` DECIMAL(15,3) NOT NULL DEFAULT 0,
    `previous_value` DECIMAL(15,3) NOT NULL DEFAULT 0,
    `quantity` DECIMAL(15,3) NOT NULL DEFAULT 0,
    `evidence_url` TEXT NULL,
    `note` VARCHAR(500) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`reading_id`),
    KEY `idx_meter_reading_meter_id` (`meter_id`),
    KEY `idx_meter_reading_meter_date` (`meter_id`, `reading_at`),

    CONSTRAINT `fk_meter_reading_meter`
        FOREIGN KEY (`meter_id`) REFERENCES `meter` (`meter_id`)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `ck_meter_reading_current`
        CHECK (`current_value` >= 0),

    CONSTRAINT `ck_meter_reading_previous`
        CHECK (`previous_value` >= 0),

    CONSTRAINT `ck_meter_reading_quantity`
        CHECK (`quantity` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 9. MAINTENANCE REQUESTS
-- ============================================================

CREATE TABLE IF NOT EXISTS `maintenance_requests` (
    `request_id` BINARY(16) NOT NULL,
    `user_id` BINARY(16) NOT NULL,
    `room_id` BINARY(16) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `priority` VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    `completed_at` DATETIME(3) NULL,

    PRIMARY KEY (`request_id`),
    KEY `idx_maintenance_user_id` (`user_id`),
    KEY `idx_maintenance_room_id` (`room_id`),
    KEY `idx_maintenance_status` (`status`),

    CONSTRAINT `fk_maintenance_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `fk_maintenance_room`
        FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `request_image` (
    `image_id` BINARY(16) NOT NULL,
    `request_id` BINARY(16) NOT NULL,
    `file_url` TEXT NOT NULL,
    `image_type` VARCHAR(50) NULL,

    PRIMARY KEY (`image_id`),
    KEY `idx_request_image_request_id` (`request_id`),

    CONSTRAINT `fk_request_image_request`
        FOREIGN KEY (`request_id`) REFERENCES `maintenance_requests` (`request_id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 10. INVOICES
-- All monetary amounts are VND.
-- ============================================================

CREATE TABLE IF NOT EXISTS `invoice` (
    `invoice_id` BINARY(16) NOT NULL,
    `contract_id` BINARY(16) NOT NULL,
    `invoice_number` VARCHAR(100) NOT NULL,
    `issued_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `due_date` DATE NULL,

    `subtotal` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND',
    `discount_amount` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND',
    `tax_amount` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND',
    `total_amount` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND',

    `note` TEXT NULL,
    `status` ENUM('SENTED', 'RECEIVED') NOT NULL DEFAULT 'SENTED',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`invoice_id`),
    UNIQUE KEY `uq_invoice_number` (`invoice_number`),
    KEY `idx_invoice_contract_id` (`contract_id`),
    KEY `idx_invoice_due_date` (`due_date`),
    KEY `idx_invoice_issued_at` (`issued_at`),
    KEY `idx_invoice_status` (`status`),

    CONSTRAINT `fk_invoice_contract`
        FOREIGN KEY (`contract_id`) REFERENCES `rental_contracts` (`contract_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `ck_invoice_subtotal`
        CHECK (`subtotal` >= 0),
    CONSTRAINT `ck_invoice_discount`
        CHECK (`discount_amount` >= 0),
    CONSTRAINT `ck_invoice_tax`
        CHECK (`tax_amount` >= 0),
    CONSTRAINT `ck_invoice_total`
        CHECK (`total_amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `invoice_item` (
    `item_id` BINARY(16) NOT NULL,
    `invoice_id` BINARY(16) NOT NULL,
    `service_id` BINARY(16) NOT NULL,
    `reading_id` BINARY(16) NULL,
    `description` VARCHAR(500) NULL,
    `unit` VARCHAR(50) NULL,
    `unit_price` DECIMAL(15,0) NOT NULL DEFAULT 0 COMMENT 'VND/unit',
    `amount` DECIMAL(15,3) NOT NULL DEFAULT 0,

    PRIMARY KEY (`item_id`),
    KEY `idx_invoice_item_invoice_id` (`invoice_id`),
    KEY `idx_invoice_item_service_id` (`service_id`),
    KEY `idx_invoice_item_reading_id` (`reading_id`),

    CONSTRAINT `fk_invoice_item_invoice`
        FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`invoice_id`)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `fk_invoice_item_service`
        FOREIGN KEY (`service_id`) REFERENCES `service` (`service_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `fk_invoice_item_reading`
        FOREIGN KEY (`reading_id`) REFERENCES `meter_reading` (`reading_id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `ck_invoice_item_unit_price`
        CHECK (`unit_price` >= 0),
    CONSTRAINT `ck_invoice_item_amount`
        CHECK (`amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 11. ONE REPRESENTATIVE TENANT PER CONTRACT
-- ============================================================

DROP TRIGGER IF EXISTS `trg_contract_tenant_representative_insert`;
DROP TRIGGER IF EXISTS `trg_contract_tenant_representative_update`;

DELIMITER $$

CREATE TRIGGER `trg_contract_tenant_representative_insert`
BEFORE INSERT ON `contract_tenants`
FOR EACH ROW
BEGIN
    IF NEW.`is_representative` = TRUE
       AND EXISTS (
           SELECT 1
           FROM `contract_tenants`
           WHERE `contract_id` = NEW.`contract_id`
             AND `is_representative` = TRUE
       )
    THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT =
                'A rental contract can have only one representative tenant';
    END IF;
END$$

CREATE TRIGGER `trg_contract_tenant_representative_update`
BEFORE UPDATE ON `contract_tenants`
FOR EACH ROW
BEGIN
    IF NEW.`is_representative` = TRUE
       AND EXISTS (
           SELECT 1
           FROM `contract_tenants`
           WHERE `contract_id` = NEW.`contract_id`
             AND `tenant_id` <> NEW.`tenant_id`
             AND `is_representative` = TRUE
       )
    THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT =
                'A rental contract can have only one representative tenant';
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- 12. INITIAL VIETNAMESE MASTER DATA
-- ============================================================

INSERT INTO `role`
    (`role_id`, `role_name`, `description`, `status`)
VALUES
    (UUID_TO_BIN('00000000-0000-4000-8000-000000000002'), 'LANDLORD', 'Chủ nhà / người quản lý nhà trọ', 'ACTIVE'),
    (UUID_TO_BIN('00000000-0000-4000-8000-000000000003'), 'TENANT', 'Người thuê phòng', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    `description` = VALUES(`description`),
    `status` = VALUES(`status`);

INSERT INTO `service`
    (`service_id`, `name`, `unit`, `calculation_method`, `default_unit_price`)
VALUES
    (UUID_TO_BIN('00000000-0000-4000-8000-000000000011'), 'Điện', 'kWh', 'METER', 0),
    (UUID_TO_BIN('00000000-0000-4000-8000-000000000012'), 'Nước', 'm3', 'METER', 0),
    (UUID_TO_BIN('00000000-0000-4000-8000-000000000013'), 'Internet', 'tháng', 'FIXED', 0),
    (UUID_TO_BIN('00000000-0000-4000-8000-000000000014'), 'Phí vệ sinh', 'tháng', 'FIXED', 0),
    (UUID_TO_BIN('00000000-0000-4000-8000-000000000015'), 'Phí giữ xe', 'xe/tháng', 'FIXED', 0)
ON DUPLICATE KEY UPDATE
    `unit` = VALUES(`unit`),
    `calculation_method` = VALUES(`calculation_method`),
    `default_unit_price` = VALUES(`default_unit_price`);

SET FOREIGN_KEY_CHECKS = 1;
