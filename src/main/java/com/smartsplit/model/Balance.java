package com.smartsplit.model;

import java.math.BigDecimal;

/** A participant's net position within one expense group. */
public record Balance(Member member, BigDecimal amount) {
    public boolean isCreditor() {
        return amount.signum() > 0;
    }

    public boolean isDebtor() {
        return amount.signum() < 0;
    }
}
