package com.smartsplit.strategy;

import com.smartsplit.exception.InvalidSplitException;
import com.smartsplit.model.Member;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Equal allocation that assigns any cent-level remainder to the final participant. */
public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public Map<Integer, BigDecimal> calculate(BigDecimal total,
                                              List<Member> members,
                                              Map<Integer, BigDecimal> ignored) {
        if (total == null || total.signum() <= 0) {
            throw new InvalidSplitException("Expense total must be greater than zero.");
        }
        if (members == null || members.isEmpty()) {
            throw new InvalidSplitException("At least one member is required.");
        }

        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal baseShare = total.divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.DOWN);
        BigDecimal assigned = BigDecimal.ZERO;
        for (int i = 0; i < members.size(); i++) {
            BigDecimal share = i == members.size() - 1 ? total.subtract(assigned) : baseShare;
            result.put(members.get(i).getId(), share);
            assigned = assigned.add(share);
        }
        return result;
    }

    @Override
    public String getName() {
        return "EQUAL";
    }
}
