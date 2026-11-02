package com.smartsplit.util;

import com.smartsplit.exception.InvalidAmountException;
import com.smartsplit.exception.InvalidSplitException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/** Shared validation helpers for service-layer use cases. */
public final class Validator {
    private Validator() { }

    public static String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    public static BigDecimal positiveAmount(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidAmountException("Enter an amount greater than zero.");
        }
        try {
            BigDecimal amount = new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
            if (amount.signum() <= 0) {
                throw new InvalidAmountException("Amount must be greater than zero.");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new InvalidAmountException("Enter a valid monetary amount.");
        }
    }

    public static void nonEmpty(Collection<?> values, String message) {
        if (values == null || values.isEmpty()) {
            throw new InvalidSplitException(message);
        }
    }

    public static void percentagesTotal100(Collection<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            throw new InvalidSplitException("Percentage split needs participant percentages.");
        }
        BigDecimal total = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        if (total.compareTo(new BigDecimal("100.00")) != 0) {
            throw new InvalidSplitException("Percentages must total exactly 100%.");
        }
    }
}
