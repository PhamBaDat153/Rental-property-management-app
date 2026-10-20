package com.android_dev.rentaly_management.Util;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtils {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateUtils() {
    }

    public static String formatDate(@Nullable LocalDate date) {
        return date == null ? "Chưa cập nhật" : date.format(DATE_FORMAT);
    }

    public static String formatDateOrOpen(@Nullable LocalDate date) {
        return date == null ? "Chưa xác định" : date.format(DATE_FORMAT);
    }

    public static String formatDateTime(@Nullable LocalDateTime dateTime) {
        return dateTime == null ? "Chưa cập nhật" : dateTime.format(DATE_TIME_FORMAT);
    }

    @Nullable
    public static LocalDate parseDate(@Nullable String text) {
        if (text == null || text.trim().isEmpty()
                || "Chưa cập nhật".equals(text.trim())
                || "Chưa xác định".equals(text.trim())) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException ignored) {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        }
    }
}
