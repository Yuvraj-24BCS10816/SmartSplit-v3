package com.smartsplit.strategy;

import com.smartsplit.exception.InvalidSplitException;
import com.smartsplit.model.Member;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Uses entered amounts and rejects allocations that do not equal the expense total. */
public class ExactSplitStrategy implements SplitStrategy {
    @Override
    public Map<Integer, BigDecimal> calculate(BigDecimal total,
                                              List<Member> members,
                                              Map<Integer, BigDecimal> values) {
        if (total == null || total.signum() <= 0) {
            throw new InvalidSplitException("Expense total must be greater than zero.");
        }
        if (members == null || members.isEmpty() || values == null) {
            throw new InvalidSplitException("Exact split needs participant amounts.");
        }

        BigDecimal sum = BigDecimal.ZERO;
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        for (Member member : members) {
            BigDecimal value = values.get(member.getId());
            if (value == null || value.signum() < 0) {
                throw new InvalidSplitException("Every participant needs a non-negative exact amount.");
            }
            value = value.setScale(2, RoundingMode.HALF_UP);
            result.put(member.getId(), value);
            sum = sum.add(value);
        }

        if (sum.compareTo(total.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new InvalidSplitException("Exact split amounts must add up to the expense total.");
        }
        return result;
    }

    @Override
    public String getName() {
        return "EXACT";
    }
}
