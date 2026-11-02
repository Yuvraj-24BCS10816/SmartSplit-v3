package com.smartsplit.strategy;

import com.smartsplit.model.Member;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Common contract for interchangeable expense allocation algorithms. */
public interface SplitStrategy {
    Map<Integer, BigDecimal> calculate(BigDecimal total,
                                       List<Member> members,
                                       Map<Integer, BigDecimal> values);

    default Map<Integer, BigDecimal> percentages() {
        return Collections.emptyMap();
    }

    String getName();
}
