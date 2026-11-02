package com.smartsplit.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Consistent Indian-currency formatting for every screen and report. */
public final class FormatUtil {
    private static final Locale INDIA = Locale.of("en", "IN");

    private FormatUtil() { }

    public static String money(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        NumberFormat format = NumberFormat.getCurrencyInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value.abs()).replace("₹", value.signum() < 0 ? "−₹" : "₹");
    }

    public static String shortMoney(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        NumberFormat format = NumberFormat.getNumberInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        String formatted = format.format(value.abs());
        return (value.signum() < 0 ? "−₹" : "₹") + formatted;
    }
}
