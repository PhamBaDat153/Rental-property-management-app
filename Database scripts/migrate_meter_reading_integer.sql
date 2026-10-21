-- Chuyển chỉ số đồng hồ sang số nguyên (BIGINT).
-- Dữ liệu DECIMAL cũ sẽ được MySQL làm tròn khi chuyển đổi.
ALTER TABLE `meter_reading`
    MODIFY `current_value` BIGINT NOT NULL DEFAULT 0,
    MODIFY `previous_value` BIGINT NOT NULL DEFAULT 0,
    MODIFY `quantity` BIGINT NOT NULL DEFAULT 0;
