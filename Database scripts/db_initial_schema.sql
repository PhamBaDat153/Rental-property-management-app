CREATE DATABASE IF NOT EXISTS rental_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE rental_management;

-- =============================================================================
-- Application users
-- =============================================================================

-- BANG: users
-- Du lieu luu tru:
--   Tai khoan dang nhap cua chu tro va nguoi thue, gom so dien thoai, email,
--   mat khau da bam, ho ten hien thi, anh dai dien, vai tro va trang thai.
-- Nghiep vu:
--   Phuc vu dang ky, dang nhap, dang xuat, doi mat khau, cap nhat tai khoan
--   va phan biet quyen landlord/tenant. Day la danh tinh xac thuc, khong phai
--   ho so phap ly cua nguoi thue; ho so do duoc luu rieng trong tenants.
-- Quan he:
--   Mot user vai tro tenant co the lien ket toi toi da mot tenants.user_id.
--   Nhieu bang su dung user lam nguoi tao, nguoi ghi nhan hoac nguoi xu ly.
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

-- BANG: properties
-- Du lieu luu tru:
--   Danh sach cac khu/dia chi nha tro do mot chu tro quan ly, bao gom ma khu,
--   ten, dia chi hien thi, don vi hanh chinh, toa do va trang thai hoat dong.
-- Nghiep vu:
--   Cho phep mot chu tro quan ly nhieu dia chi va loc phong, dich vu, doanh thu,
--   cong no theo tung khu. deleted_at dung de xoa mem va giu du lieu lich su.
-- Quan he:
--   Mot property co nhieu rooms va services; announcement cung co the nham
--   truc tiep den toan bo nguoi thue cua mot property.
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

-- BANG: rooms
-- Du lieu luu tru:
--   Thong tin tung phong: khu tro, ma phong, ten, tang, dien tich, suc chua,
--   gia thue/goi y tien coc, mo ta va trang thai van hanh.
-- Nghiep vu:
--   Ho tro them, sua, xem va xoa mem phong. Trang thai phong dang duoc thue
--   khong luu truc tiep o day ma duoc suy ra tu rental_contracts dang active.
-- Quan he:
--   Moi room thuoc mot property; mot room co nhieu hop dong theo thoi gian,
--   cong to va yeu cau sua chua. Ma phong chi duy nhat trong cung mot property.
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

-- BANG: tenants
-- Du lieu luu tru:
--   Ho so nghiep vu/phap ly cua nguoi thue: ho ten, lien lac, ngay sinh, CCCD
--   hoac ho chieu, dia chi thuong tru, lien he khan cap va ghi chu.
-- Nghiep vu:
--   Chu tro co the tao ho so truoc khi nguoi thue dang ky tai khoan. user_id
--   de NULL den khi dang ky bang ma moi. So giay to duoc ma hoa; hash duoc dung
--   de phat hien trung lap ma khong can tim tren du lieu ro.
-- Quan he:
--   Co the lien ket 0..1 user; tham gia nhieu hop dong qua contract_tenants;
--   co the la nguoi thanh toan, nguoi bao sua chua va nguoi nhan thong bao.
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

-- BANG: tenant_invitations
-- Du lieu luu tru:
--   Ma moi dang ky da bam, tenant duoc moi, so dien thoai tai thoi diem moi,
--   thoi han, thoi diem su dung/thu hoi, nguoi tao va user da su dung ma.
-- Nghiep vu:
--   Chi cho phep nguoi co ma moi hop le va so dien thoai khop ho so hoan tat
--   dang ky. Khong luu ma moi dang ro; cac moc thoi gian cho phep suy ra trang
--   thai active, expired, used hoac revoked va giu duoc lich su cap ma.
-- Quan he:
--   Moi invitation thuoc mot tenant, duoc tao boi landlord user va co the duoc
--   su dung boi mot tenant user sau khi dang ky thanh cong.
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

-- BANG: rental_contracts
-- Du lieu luu tru:
--   Hop dong thue cua mot phong: ma hop dong, ngay bat dau/ket thuc, ngay ky,
--   tien phong, tien coc, ngay lap hoa don, han thanh toan, dieu khoan, tai lieu,
--   trang thai va thong tin cham dut.
-- Nghiep vu:
--   Quan ly vong doi draft -> active -> expired/terminated/cancelled, lam co so
--   gan nguoi thue, ap dung dich vu, lap hoa don va theo doi hop dong sap het han.
-- Quan he:
--   Moi contract thuoc mot room va duoc tao boi mot user; co nhieu occupants,
--   lan gia han, dich vu, hoa don va yeu cau sua chua.
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

-- BANG: contract_tenants
-- Du lieu luu tru:
--   Thanh vien tham gia hop dong, vai tro nguoi dai dien, ngay vao/o ra va
--   trang thai dang o hay da chuyen di.
-- Nghiep vu:
--   Thuc hien viec gan mot hoac nhieu nguoi thue vao phong thong qua hop dong,
--   dong thoi giu lich su cu tru cua tung nguoi thay vi gan tenant vao room.
-- Quan he:
--   Bang trung gian nhieu-nhieu giua rental_contracts va tenants; mot tenant
--   chi xuat hien mot lan trong cung mot contract.
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

-- BANG: contract_extensions
-- Du lieu luu tru:
--   Lich su moi lan gia han gom ngay ket thuc cu, ngay ket thuc moi, ghi chu,
--   nguoi thuc hien va thoi diem tao.
-- Nghiep vu:
--   Cho phep cap nhat end_date hien tai cua hop dong nhung van truy vet duoc
--   tat ca lan gia han; new_end_date bat buoc lon hon old_end_date.
-- Quan he:
--   Nhieu extension thuoc mot rental_contract va duoc tao boi mot user.
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

-- BANG: services
-- Du lieu luu tru:
--   Danh muc dich vu cua tung khu tro nhu dien, nuoc, WiFi, rac, gui xe,
--   ve sinh; gom cach tinh, don vi, don gia mac dinh va trang thai ap dung.
-- Nghiep vu:
--   Cau hinh cac khoan phi theo tung property. calculation_method quy dinh
--   tinh theo cong to, co dinh, so nguoi, so xe hoac so luong tuy chinh.
-- Quan he:
--   Moi service thuoc mot property, co the duoc gan vao nhieu hop dong,
--   gan voi meters va duoc tham chieu boi cac dong chi tiet hoa don.
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

-- BANG: contract_services
-- Du lieu luu tru:
--   Dich vu thuc te ap dung cho mot hop dong, so luong, don gia da thoa thuan,
--   khoang ngay hieu luc va trang thai hien tai.
-- Nghiep vu:
--   Dong bang don gia thoa thuan rieng cho tung hop dong. Khi thay doi gia,
--   dong cu duoc dong bang end_date va tao dong moi de khong mat lich su gia.
-- Quan he:
--   Lien ket rental_contracts voi services; mot dich vu co the co nhieu giai
--   doan gia trong cung hop dong nhung khong trung contract/service/start_date.
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

-- BANG: meters
-- Du lieu luu tru:
--   Cong to dien/nuoc gan voi phong: dich vu do, so serial, ngay lap, chi so
--   ban dau, ngay thao va trang thai active/replaced/broken/inactive.
-- Nghiep vu:
--   Quan ly vong doi cong to. Khi thay cong to, dong cu duoc danh dau replaced
--   va tao dong moi voi initial_value rieng de lich su chi so khong bi dut gay.
-- Quan he:
--   Moi meter thuoc mot room va mot service co cach tinh metered; co nhieu
--   meter_readings theo thoi gian.
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

-- BANG: meter_readings
-- Du lieu luu tru:
--   Tung lan chot chi so cong to: thoi diem, gia tri, anh bang chung, nguoi ghi
--   nhan, ghi chu va thoi diem tao ban ghi.
-- Nghiep vu:
--   Luu lich su dien/nuoc. Luong tieu thu duoc tinh bang chi so hien tai tru
--   chi so lien truoc (hoac initial_value), tranh luu lap previous/current.
-- Quan he:
--   Nhieu reading thuoc mot meter; invoice_items co the tham chieu reading dau
--   va reading cuoi de giai thich khoan tien dien/nuoc.
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

-- BANG: invoices
-- Du lieu luu tru:
--   Hoa don cua mot hop dong theo ky: so hoa don, tu ngay/den ngay, ngay phat
--   hanh, han thanh toan, tam tinh, giam gia, thue, tong tien va trang thai.
-- Nghiep vu:
--   Moi hop dong chi co mot hoa don cho cung mot ky. draft co the chinh sua;
--   issued la chung tu da phat hanh; cancelled giu lich su hoa don bi huy.
-- Quan he:
--   Moi invoice thuoc mot rental_contract, co nhieu invoice_items va payments.
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

-- BANG: invoice_items
-- Du lieu luu tru:
--   Chi tiet tung khoan tren hoa don: tien phong, dich vu, dien, nuoc, giam gia,
--   thue/dieu chinh; gom mo ta, so luong, don vi, don gia, thanh tien va JSON
--   mo ta cach tinh. Co the luu hai chi so cong to nguon.
-- Nghiep vu:
--   La snapshot tai chinh tai luc lap hoa don. Du lieu van giu nguyen khi ten,
--   don gia dich vu hay hop dong thay doi ve sau.
-- Quan he:
--   Moi item thuoc mot invoice; tuy loai co the tham chieu service va cap
--   meter_readings bat dau/ket thuc.
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

-- BANG: payments
-- Du lieu luu tru:
--   Giao dich thanh toan: hoa don, nguoi tra, so tien, phuong thuc, ma tham
--   chieu, thoi diem tra, trang thai, nguoi nhan, anh minh chung va ghi chu.
-- Nghiep vu:
--   Moi payment chi thanh toan cho mot invoice; mot invoice co the duoc tra
--   nhieu lan de ho tro thanh toan mot phan. Chi payment confirmed duoc tinh
--   vao doanh thu va so tien da thanh toan; cancelled/refunded van giu lich su.
-- Quan he:
--   Thuoc bat buoc mot invoice; co the ghi nhan payer tenant va receiver user.
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

-- BANG: maintenance_requests
-- Du lieu luu tru:
--   Yeu cau sua chua cua phong: hop dong va tenant bao loi, tieu de, mo ta,
--   muc uu tien, trang thai, nguoi duoc giao va cac moc thoi gian xu ly.
-- Nghiep vu:
--   Ho tro gui, tiep nhan, xu ly, cho doi, hoan thanh, huy, tu choi va xac nhan
--   hoan thanh. Cot status luu trang thai hien tai de hien thi danh sach nhanh.
-- Quan he:
--   Thuoc mot room, co the gan contract/tenant/assignee; co nhieu event va file.
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

-- BANG: maintenance_request_events
-- Du lieu luu tru:
--   Nhat ky bat bien cua yeu cau sua chua: loai su kien, trang thai truoc/sau,
--   ghi chu, nguoi thuc hien va thoi diem.
-- Nghiep vu:
--   Bao toan lich su xu ly thay vi chi ghi de status trong maintenance_requests;
--   ho tro chu tro va nguoi thue xem dien bien va ghi chu cua tung buoc.
-- Quan he:
--   Nhieu event thuoc mot maintenance_request va moi event co mot actor user.
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

-- BANG: maintenance_attachments
-- Du lieu luu tru:
--   Tep dinh kem cua yeu cau sua chua, gom URL tep, loai image/video/document,
--   nguoi tai len va thoi diem tai.
-- Nghiep vu:
--   Luu anh hien trang, video loi hoac tai lieu xu ly. Database chi luu URL,
--   khong luu truc tiep noi dung nhi phan de tranh phinh kich thuoc.
-- Quan he:
--   Nhieu attachment thuoc mot maintenance_request va mot uploader user.
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

-- BANG: announcements
-- Du lieu luu tru:
--   Noi dung thong bao, pham vi nguoi nhan, doi tuong property/room/contract
--   neu co, trang thai draft/scheduled/sending/sent/cancelled va lich gui.
-- Nghiep vu:
--   Chu tro tao va gui thong bao den tat ca, mot khu, mot phong, mot hop dong
--   hoac danh sach tenant chon rieng. CHECK dam bao moi target_type chi su dung
--   dung mot loai khoa ngoai muc tieu.
-- Quan he:
--   Co the tham chieu property, room hoac contract; do mot user tao va co nhieu
--   announcement_recipients sau khi danh sach nguoi nhan duoc chot.
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

-- BANG: announcement_recipients
-- Du lieu luu tru:
--   Snapshot tung nguoi nhan cua thong bao, tai khoan lien ket, trang thai gui,
--   thoi diem gui/nhan/doc va ly do that bai.
-- Nghiep vu:
--   Theo doi lich su gui va danh dau da doc rieng cho tung tenant. Snapshot
--   khong thay doi neu tenant chuyen phong sau khi thong bao da duoc gui.
-- Quan he:
--   Moi dong lien ket mot announcement voi mot tenant va user tuy chon; mot
--   tenant chi co mot dong nhan trong cung mot announcement.
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
