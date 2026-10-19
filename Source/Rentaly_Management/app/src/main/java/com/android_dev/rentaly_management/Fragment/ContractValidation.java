package com.android_dev.rentaly_management.Fragment;

import java.math.BigDecimal;
import java.time.LocalDate;

final class ContractValidation {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final int MIN_BILLING_DAY = 1;
    private static final int MAX_BILLING_DAY = 31;
    private static final int MIN_PAYMENT_DUE_DAYS = 0;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    private ContractValidation() {
        // Prevent instantiation.
    }

    // -------------------------------------------------------------------------
    // Contract validation
    // -------------------------------------------------------------------------

    /**
     * Validates the main fields of a rental contract.
     */
    static boolean valid(
            LocalDate start,
            LocalDate end,
            BigDecimal rent,
            BigDecimal deposit,
            Integer billingDay,
            Integer paymentDueDays
    ) {
        return hasValidDates(start, end)
                && hasValidAmounts(rent, deposit)
                && hasValidPaymentSettings(billingDay, paymentDueDays);
    }

    // -------------------------------------------------------------------------
    // Date validation
    // -------------------------------------------------------------------------

    /**
     * Checks whether the contract dates are valid.
     * The end date is optional but cannot be earlier than the start date.
     */
    private static boolean hasValidDates(
            LocalDate start,
            LocalDate end
    ) {
        if (start == null) {
            return false;
        }

        return end == null || !end.isBefore(start);
    }

    // -------------------------------------------------------------------------
    // Amount validation
    // -------------------------------------------------------------------------

    /**
     * Ensures that the rental amount and deposit are non-negative.
     */
    private static boolean hasValidAmounts(
            BigDecimal rent,
            BigDecimal deposit
    ) {
        return rent != null
                && deposit != null
                && rent.signum() >= 0
                && deposit.signum() >= 0;
    }

    // -------------------------------------------------------------------------
    // Payment validation
    // -------------------------------------------------------------------------

    /**
     * Validates the billing day and payment due period.
     */
    private static boolean hasValidPaymentSettings(
            Integer billingDay,
            Integer paymentDueDays
    ) {
        return isValidBillingDay(billingDay)
                && isValidPaymentDueDays(paymentDueDays);
    }

    /**
     * Checks whether the billing day is within the allowed range.
     * A null value is allowed.
     */
    private static boolean isValidBillingDay(Integer billingDay) {
        return billingDay == null
                || (billingDay >= MIN_BILLING_DAY
                && billingDay <= MAX_BILLING_DAY);
    }

    /**
     * Ensures that the payment due period is non-negative.
     */
    private static boolean isValidPaymentDueDays(Integer paymentDueDays) {
        return paymentDueDays != null
                && paymentDueDays >= MIN_PAYMENT_DUE_DAYS;
    }
}