package com.android_dev.rentaly_management.Fragment;

import java.math.BigDecimal;
import java.time.LocalDate;

final class ContractValidation {
    private ContractValidation() { }

    static boolean valid(LocalDate start, LocalDate end, BigDecimal rent, BigDecimal deposit,
                         Integer billingDay, Integer paymentDueDays) {
        return start != null && rent != null && deposit != null
                && rent.signum() >= 0 && deposit.signum() >= 0
                && paymentDueDays != null && paymentDueDays >= 0
                && (billingDay == null || billingDay >= 1 && billingDay <= 31)
                && (end == null || !end.isBefore(start));
    }
}
