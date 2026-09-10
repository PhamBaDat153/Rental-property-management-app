CREATE DATABASE IF NOT EXISTS rental_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE rental_management;

-- =============================================================================
-- Application users
-- =============================================================================

CREATE TABLE users (
  id BINARY(16) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  email VARCHAR(255) NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(150) NOT NULL,
  avatar_url VARCHAR(500) NULL,
  role ENUM('landlord', 'tenant') NOT NULL,
  status ENUM('active', 'locked', 'disabled') NOT NULL DEFAULT 'active',
  last_login_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_phone (phone),
  UNIQUE KEY uq_users_email (email)
) ENGINE = InnoDB;

-- =============================================================================
-- Property inventory
-- =============================================================================

CREATE TABLE properties (
  id BINARY(16) NOT NULL,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(200) NOT NULL,
  address_line VARCHAR(500) NOT NULL,
  province_code VARCHAR(20) NULL,
  province_name VARCHAR(100) NULL,
  ward_code VARCHAR(20) NULL,
  ward_name VARCHAR(100) NULL,
  latitude DECIMAL(10, 7) NULL,
  longitude DECIMAL(10, 7) NULL,
  description TEXT NULL,
  status ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_properties_code (code),
  KEY ix_properties_status (status),
  CONSTRAINT ck_properties_latitude CHECK (
    latitude IS NULL
    OR latitude BETWEEN -90 AND 90
  ),
  CONSTRAINT ck_properties_longitude CHECK (
    longitude IS NULL
    OR longitude BETWEEN -180 AND 180
  )
) ENGINE = InnoDB;

CREATE TABLE rooms (
  id BINARY(16) NOT NULL,
  property_id BINARY(16) NOT NULL,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(150) NULL,
  floor SMALLINT NULL,
  area_m2 DECIMAL(8, 2) NULL,
  max_occupants SMALLINT UNSIGNED NOT NULL,
  suggested_rent DECIMAL(15, 2) NOT NULL DEFAULT 0,
  suggested_deposit DECIMAL(15, 2) NOT NULL DEFAULT 0,
  operational_status ENUM('available', 'maintenance', 'inactive') NOT NULL DEFAULT 'available',
  description TEXT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_rooms_property_code (property_id, code),
  KEY ix_rooms_property_status (property_id, operational_status),
  CONSTRAINT fk_rooms_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE RESTRICT,
  CONSTRAINT ck_rooms_occupants CHECK (max_occupants > 0),
  CONSTRAINT ck_rooms_rent CHECK (suggested_rent >= 0),
  CONSTRAINT ck_rooms_deposit CHECK (suggested_deposit >= 0)
) ENGINE = InnoDB;

-- =============================================================================
-- Tenant profiles and onboarding
-- =============================================================================

CREATE TABLE tenants (
  id BINARY(16) NOT NULL,
  user_id BINARY(16) NULL,
  full_name VARCHAR(150) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  email VARCHAR(255) NULL,
  date_of_birth DATE NULL,
  gender ENUM('male', 'female', 'other', 'unknown') NOT NULL DEFAULT 'unknown',
  identity_type ENUM('cccd', 'passport', 'other') NULL,
  identity_number_encrypted VARBINARY(512) NULL,
  identity_number_hash BINARY(32) NULL,
  identity_issued_date DATE NULL,
  identity_issued_place VARCHAR(255) NULL,
  permanent_address VARCHAR(500) NULL,
  emergency_contact_name VARCHAR(150) NULL,
  emergency_contact_phone VARCHAR(20) NULL,
  note TEXT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at DATETIME(3) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_tenants_user (user_id),
  KEY ix_tenants_phone (phone),
  KEY ix_tenants_identity_hash (identity_number_hash),
  CONSTRAINT fk_tenants_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE tenant_invitations (
  id BINARY(16) NOT NULL,
  tenant_id BINARY(16) NOT NULL,
  invited_phone VARCHAR(20) NOT NULL,
  token_hash BINARY(32) NOT NULL,
  expires_at DATETIME(3) NOT NULL,
  used_at DATETIME(3) NULL,
  used_by_user_id BINARY(16) NULL,
  revoked_at DATETIME(3) NULL,
  created_by_user_id BINARY(16) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_invitations_token_hash (token_hash),
  KEY ix_invitations_tenant_state (tenant_id, used_at, revoked_at),
  KEY ix_invitations_expiry (expires_at),
  CONSTRAINT fk_invitations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT,
  CONSTRAINT fk_invitations_used_by FOREIGN KEY (used_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT fk_invitations_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

-- =============================================================================
-- Contracts and services
-- =============================================================================

CREATE TABLE rental_contracts (
  id BINARY(16) NOT NULL,
  room_id BINARY(16) NOT NULL,
  contract_number VARCHAR(50) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NULL,
  signed_at DATETIME(3) NULL,
  rent_amount DECIMAL(15, 2) NOT NULL,
  deposit_required DECIMAL(15, 2) NOT NULL DEFAULT 0,
  billing_day TINYINT UNSIGNED NOT NULL,
  payment_due_days TINYINT UNSIGNED NOT NULL DEFAULT 0,
  status ENUM(
    'draft',
    'active',
    'expired',
    'terminated',
    'cancelled'
  ) NOT NULL DEFAULT 'draft',
  terms LONGTEXT NULL,
  document_url VARCHAR(500) NULL,
  terminated_at DATETIME(3) NULL,
  termination_reason VARCHAR(1000) NULL,
  created_by_user_id BINARY(16) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_contracts_number (contract_number),
  KEY ix_contracts_room_dates (room_id, status, start_date, end_date),
  KEY ix_contracts_status_end (status, end_date),
  CONSTRAINT fk_contracts_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE RESTRICT,
  CONSTRAINT fk_contracts_creator FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT ck_contracts_dates CHECK (
    end_date IS NULL
    OR end_date >= start_date
  ),
  CONSTRAINT ck_contracts_amounts CHECK (
    rent_amount >= 0
    AND deposit_required >= 0
  ),
  CONSTRAINT ck_contracts_billing_day CHECK (billing_day BETWEEN 1 AND 28)
) ENGINE = InnoDB;

CREATE TABLE contract_tenants (
  id BINARY(16) NOT NULL,
  contract_id BINARY(16) NOT NULL,
  tenant_id BINARY(16) NOT NULL,
  is_representative BOOLEAN NOT NULL DEFAULT FALSE,
  move_in_date DATE NOT NULL,
  move_out_date DATE NULL,
  status ENUM('active', 'moved_out') NOT NULL DEFAULT 'active',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_contract_tenants_member (contract_id, tenant_id),
  KEY ix_contract_tenants_tenant_status (tenant_id, status),
  CONSTRAINT fk_contract_tenants_contract FOREIGN KEY (contract_id) REFERENCES rental_contracts (id) ON DELETE RESTRICT,
  CONSTRAINT fk_contract_tenants_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT,
  CONSTRAINT ck_contract_tenants_dates CHECK (
    move_out_date IS NULL
    OR move_out_date >= move_in_date
  )
) ENGINE = InnoDB;

CREATE TABLE contract_extensions (
  id BINARY(16) NOT NULL,
  contract_id BINARY(16) NOT NULL,
  old_end_date DATE NOT NULL,
  new_end_date DATE NOT NULL,
  note VARCHAR(1000) NULL,
  created_by_user_id BINARY(16) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_contract_extensions_contract_created (contract_id, created_at),
  CONSTRAINT fk_extensions_contract FOREIGN KEY (contract_id) REFERENCES rental_contracts (id) ON DELETE RESTRICT,
  CONSTRAINT fk_extensions_creator FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT ck_extensions_dates CHECK (new_end_date > old_end_date)
) ENGINE = InnoDB;

CREATE TABLE services (
  id BINARY(16) NOT NULL,
  property_id BINARY(16) NOT NULL,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(150) NOT NULL,
  service_type ENUM(
    'electricity',
    'water',
    'wifi',
    'parking',
    'garbage',
    'cleaning',
    'other'
  ) NOT NULL,
  calculation_method ENUM(
    'metered',
    'fixed',
    'per_person',
    'per_vehicle',
    'per_unit'
  ) NOT NULL,
  unit VARCHAR(30) NOT NULL,
  default_unit_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
  is_required BOOLEAN NOT NULL DEFAULT FALSE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_services_property_code (property_id, code),
  KEY ix_services_property_type_active (property_id, service_type, is_active),
  CONSTRAINT fk_services_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE RESTRICT,
  CONSTRAINT ck_services_price CHECK (default_unit_price >= 0)
) ENGINE = InnoDB;

CREATE TABLE contract_services (
  id BINARY(16) NOT NULL,
  contract_id BINARY(16) NOT NULL,
  service_id BINARY(16) NOT NULL,
  quantity DECIMAL(15, 3) NOT NULL DEFAULT 1,
  agreed_unit_price DECIMAL(15, 2) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_contract_services_period (contract_id, service_id, start_date),
  KEY ix_contract_services_active (contract_id, is_active, start_date, end_date),
  CONSTRAINT fk_contract_services_contract FOREIGN KEY (contract_id) REFERENCES rental_contracts (id) ON DELETE RESTRICT,
  CONSTRAINT fk_contract_services_service FOREIGN KEY (service_id) REFERENCES services (id) ON DELETE RESTRICT,
  CONSTRAINT ck_contract_services_values CHECK (
    quantity >= 0
    AND agreed_unit_price >= 0
    AND (
      end_date IS NULL
      OR end_date >= start_date
    )
  )
) ENGINE = InnoDB;

-- =============================================================================
-- Metering and billing
-- =============================================================================

CREATE TABLE meters (
  id BINARY(16) NOT NULL,
  room_id BINARY(16) NOT NULL,
  service_id BINARY(16) NOT NULL,
  serial_number VARCHAR(100) NULL,
  installed_at DATE NOT NULL,
  initial_value DECIMAL(15, 3) NOT NULL DEFAULT 0,
  removed_at DATE NULL,
  status ENUM('active', 'replaced', 'broken', 'inactive') NOT NULL DEFAULT 'active',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_meters_room_service_serial (room_id, service_id, serial_number),
  KEY ix_meters_room_status (room_id, status),
  CONSTRAINT fk_meters_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE RESTRICT,
  CONSTRAINT fk_meters_service FOREIGN KEY (service_id) REFERENCES services (id) ON DELETE RESTRICT,
  CONSTRAINT ck_meters_values CHECK (
    initial_value >= 0
    AND (
      removed_at IS NULL
      OR removed_at >= installed_at
    )
  )
) ENGINE = InnoDB;

CREATE TABLE meter_readings (
  id BINARY(16) NOT NULL,
  meter_id BINARY(16) NOT NULL,
  reading_at DATETIME(3) NOT NULL,
  reading_value DECIMAL(15, 3) NOT NULL,
  image_url VARCHAR(500) NULL,
  recorded_by_user_id BINARY(16) NOT NULL,
  note VARCHAR(500) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_readings_meter_time (meter_id, reading_at),
  KEY ix_readings_meter_time (meter_id, reading_at),
  CONSTRAINT fk_readings_meter FOREIGN KEY (meter_id) REFERENCES meters (id) ON DELETE RESTRICT,
  CONSTRAINT fk_readings_recorder FOREIGN KEY (recorded_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT ck_readings_value CHECK (reading_value >= 0)
) ENGINE = InnoDB;

CREATE TABLE invoices (
  id BINARY(16) NOT NULL,
  contract_id BINARY(16) NOT NULL,
  invoice_number VARCHAR(50) NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  issued_at DATETIME(3) NULL,
  due_date DATE NOT NULL,
  subtotal DECIMAL(15, 2) NOT NULL DEFAULT 0,
  discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
  tax_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
  total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
  lifecycle_status ENUM('draft', 'issued', 'cancelled') NOT NULL DEFAULT 'draft',
  note TEXT NULL,
  created_by_user_id BINARY(16) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_invoices_number (invoice_number),
  UNIQUE KEY uq_invoices_contract_period (contract_id, period_start, period_end),
  KEY ix_invoices_status_due (lifecycle_status, due_date),
  KEY ix_invoices_contract_issued (contract_id, issued_at),
  CONSTRAINT fk_invoices_contract FOREIGN KEY (contract_id) REFERENCES rental_contracts (id) ON DELETE RESTRICT,
  CONSTRAINT fk_invoices_creator FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT ck_invoices_dates CHECK (period_end >= period_start),
  CONSTRAINT ck_invoices_total CHECK (total_amount >= 0)
) ENGINE = InnoDB;

CREATE TABLE invoice_items (
  id BINARY(16) NOT NULL,
  invoice_id BINARY(16) NOT NULL,
  service_id BINARY(16) NULL,
  meter_reading_from_id BINARY(16) NULL,
  meter_reading_to_id BINARY(16) NULL,
  item_type ENUM(
    'rent',
    'service',
    'electricity',
    'water',
    'discount',
    'tax',
    'adjustment',
    'other'
  ) NOT NULL,
  description VARCHAR(500) NOT NULL,
  quantity DECIMAL(15, 3) NOT NULL,
  unit VARCHAR(30) NOT NULL,
  unit_price DECIMAL(15, 2) NOT NULL,
  amount DECIMAL(15, 2) NOT NULL,
  calculation_details JSON NULL,
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY ix_invoice_items_invoice_sort (invoice_id, sort_order),
  CONSTRAINT fk_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE RESTRICT,
  CONSTRAINT fk_items_service FOREIGN KEY (service_id) REFERENCES services (id) ON DELETE RESTRICT,
  CONSTRAINT fk_items_reading_from FOREIGN KEY (meter_reading_from_id) REFERENCES meter_readings (id) ON DELETE RESTRICT,
  CONSTRAINT fk_items_reading_to FOREIGN KEY (meter_reading_to_id) REFERENCES meter_readings (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE payments (
  id BINARY(16) NOT NULL,
  invoice_id BINARY(16) NOT NULL,
  payer_tenant_id BINARY(16) NULL,
  amount DECIMAL(15, 2) NOT NULL,
  payment_method ENUM('cash', 'bank_transfer', 'e_wallet', 'other') NOT NULL,
  transaction_reference VARCHAR(100) NULL,
  paid_at DATETIME(3) NOT NULL,
  status ENUM('pending', 'confirmed', 'cancelled', 'refunded') NOT NULL DEFAULT 'pending',
  received_by_user_id BINARY(16) NULL,
  proof_image_url VARCHAR(500) NULL,
  note VARCHAR(1000) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_payments_invoice_status (invoice_id, status),
  KEY ix_payments_paid_status (paid_at, status),
  KEY ix_payments_payer_paid (payer_tenant_id, paid_at),
  CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE RESTRICT,
  CONSTRAINT fk_payments_payer FOREIGN KEY (payer_tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT,
  CONSTRAINT fk_payments_receiver FOREIGN KEY (received_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT ck_payments_amount CHECK (amount > 0)
) ENGINE = InnoDB;

-- =============================================================================
-- Maintenance and announcements
-- =============================================================================

CREATE TABLE maintenance_requests (
  id BINARY(16) NOT NULL,
  room_id BINARY(16) NOT NULL,
  contract_id BINARY(16) NULL,
  reported_by_tenant_id BINARY(16) NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  priority ENUM('low', 'normal', 'high', 'urgent') NOT NULL DEFAULT 'normal',
  status ENUM(
    'submitted',
    'accepted',
    'in_progress',
    'waiting',
    'completed',
    'cancelled',
    'rejected'
  ) NOT NULL DEFAULT 'submitted',
  assigned_to_user_id BINARY(16) NULL,
  submitted_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  completed_at DATETIME(3) NULL,
  tenant_confirmed_at DATETIME(3) NULL,
  cancelled_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_maintenance_room_status_time (room_id, status, submitted_at),
  KEY ix_maintenance_queue (status, priority, submitted_at),
  CONSTRAINT fk_maintenance_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE RESTRICT,
  CONSTRAINT fk_maintenance_contract FOREIGN KEY (contract_id) REFERENCES rental_contracts (id) ON DELETE RESTRICT,
  CONSTRAINT fk_maintenance_tenant FOREIGN KEY (reported_by_tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT,
  CONSTRAINT fk_maintenance_assignee FOREIGN KEY (assigned_to_user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE maintenance_request_events (
  id BINARY(16) NOT NULL,
  request_id BINARY(16) NOT NULL,
  event_type ENUM(
    'created',
    'accepted',
    'status_changed',
    'note_added',
    'assigned',
    'completed',
    'cancelled',
    'rejected',
    'tenant_confirmed'
  ) NOT NULL,
  from_status VARCHAR(30) NULL,
  to_status VARCHAR(30) NULL,
  note TEXT NULL,
  actor_user_id BINARY(16) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_maintenance_events_request_time (request_id, created_at),
  CONSTRAINT fk_maintenance_events_request FOREIGN KEY (request_id) REFERENCES maintenance_requests (id) ON DELETE RESTRICT,
  CONSTRAINT fk_maintenance_events_actor FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE maintenance_attachments (
  id BINARY(16) NOT NULL,
  request_id BINARY(16) NOT NULL,
  uploaded_by_user_id BINARY(16) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  file_type ENUM('image', 'video', 'document') NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_maintenance_attachments_request (request_id),
  CONSTRAINT fk_maintenance_attachments_request FOREIGN KEY (request_id) REFERENCES maintenance_requests (id) ON DELETE RESTRICT,
  CONSTRAINT fk_maintenance_attachments_uploader FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE announcements (
  id BINARY(16) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  target_type ENUM(
    'all',
    'property',
    'room',
    'contract',
    'selected_tenants'
  ) NOT NULL,
  property_id BINARY(16) NULL,
  room_id BINARY(16) NULL,
  contract_id BINARY(16) NULL,
  status ENUM(
    'draft',
    'scheduled',
    'sending',
    'sent',
    'cancelled'
  ) NOT NULL DEFAULT 'draft',
  scheduled_at DATETIME(3) NULL,
  sent_at DATETIME(3) NULL,
  created_by_user_id BINARY(16) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_announcements_status_sent (status, sent_at),
  CONSTRAINT fk_announcements_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE RESTRICT,
  CONSTRAINT fk_announcements_room FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE RESTRICT,
  CONSTRAINT fk_announcements_contract FOREIGN KEY (contract_id) REFERENCES rental_contracts (id) ON DELETE RESTRICT,
  CONSTRAINT fk_announcements_creator FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT ck_announcements_target CHECK (
    (
      target_type IN ('all', 'selected_tenants')
      AND property_id IS NULL
      AND room_id IS NULL
      AND contract_id IS NULL
    )
    OR (
      target_type = 'property'
      AND property_id IS NOT NULL
      AND room_id IS NULL
      AND contract_id IS NULL
    )
    OR (
      target_type = 'room'
      AND property_id IS NULL
      AND room_id IS NOT NULL
      AND contract_id IS NULL
    )
    OR (
      target_type = 'contract'
      AND property_id IS NULL
      AND room_id IS NULL
      AND contract_id IS NOT NULL
    )
  )
) ENGINE = InnoDB;

CREATE TABLE announcement_recipients (
  id BINARY(16) NOT NULL,
  announcement_id BINARY(16) NOT NULL,
  tenant_id BINARY(16) NOT NULL,
  user_id BINARY(16) NULL,
  delivery_status ENUM('pending', 'sent', 'delivered', 'failed') NOT NULL DEFAULT 'pending',
  sent_at DATETIME(3) NULL,
  delivered_at DATETIME(3) NULL,
  read_at DATETIME(3) NULL,
  failure_reason VARCHAR(500) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uq_recipients_announcement_tenant (announcement_id, tenant_id),
  KEY ix_recipients_user_read (user_id, read_at),
  CONSTRAINT fk_recipients_announcement FOREIGN KEY (announcement_id) REFERENCES announcements (id) ON DELETE RESTRICT,
  CONSTRAINT fk_recipients_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT,
  CONSTRAINT fk_recipients_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB;