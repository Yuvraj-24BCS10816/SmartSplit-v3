package com.smartsplit.strategy;

import com.smartsplit.exception.InvalidSplitException;
import com.smartsplit.model.Member;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Percentage allocation whose values must add to exactly 100 percent. */
public class PercentageSplitStrategy implements SplitStrategy {
    private Map<Integer, BigDecimal> lastPercentages = Collections.emptyMap();

    @Override
    public Map<Integer, BigDecimal> calculate(BigDecimal total,
                                              List<Member> members,
                                              Map<Integer, BigDecimal> values) {
        if (total == null || total.signum() <= 0) {
            throw new InvalidSplitException("Expense total must be greater than zero.");
        }
        if (members == null || members.isEmpty() || values == null) {
            throw new InvalidSplitException("Percentage split needs participant percentages.");
        }

        BigDecimal normalizedTotal = total.setScale(2, RoundingMode.HALF_UP);
        BigDecimal percentageTotal = BigDecimal.ZERO;
        for (Member member : members) {
            BigDecimal percentage = values.get(member.getId());
            if (percentage == null || percentage.signum() < 0) {
                throw new InvalidSplitException("Every participant needs a non-negative percentage.");
            }
            percentageTotal = percentageTotal.add(percentage);
        }
        if (percentageTotal.compareTo(new BigDecimal("100")) != 0) {
            throw new InvalidSplitException("Percentages must total exactly 100%.");
        }

        lastPercentages = new LinkedHashMap<>(values);
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal assigned = BigDecimal.ZERO;
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            // Round earlier participants down so cent-level rounding can never
            // make the final participant's remainder negative. The final share
            // receives the exact remainder, preserving the original total.
            BigDecimal share = i == members.size() - 1
                    ? normalizedTotal.subtract(assigned)
                    : normalizedTotal.multiply(values.get(member.getId())).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
            result.put(member.getId(), share);
            assigned = assigned.add(share);
        }
        return result;
    }

    @Override
    public Map<Integer, BigDecimal> percentages() {
        return lastPercentages;
    }

    @Override
    public String getName() {
        return "PERCENTAGE";
    }
}
