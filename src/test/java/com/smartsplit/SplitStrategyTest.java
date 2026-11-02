package com.smartsplit;

import com.smartsplit.model.Member;
import com.smartsplit.strategy.EqualSplitStrategy;
import com.smartsplit.strategy.ExactSplitStrategy;
import com.smartsplit.strategy.PercentageSplitStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SplitStrategyTest {
    private List<Member> members() {
        return List.of(
                new Member(1, "A", "a", "a@x"),
                new Member(2, "B", "b", "b@x"),
                new Member(3, "C", "c", "c@x")
        );
    }

    @Test void equalSplitPreservesTotal() {
        var r = new EqualSplitStrategy().calculate(new BigDecimal("100.00"), members(), Map.of());
        assertEquals(new BigDecimal("100.00"), r.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test void equalSplitKeepsRoundingRemainder() {
        var r = new EqualSplitStrategy().calculate(new BigDecimal("100.00"), members(), Map.of());
        assertEquals(new BigDecimal("33.33"), r.get(1));
        assertEquals(new BigDecimal("33.33"), r.get(2));
        assertEquals(new BigDecimal("33.34"), r.get(3));
    }

    @Test void equalSplitRejectsEmptyMembers() {
        assertThrows(RuntimeException.class, () ->
                new EqualSplitStrategy().calculate(new BigDecimal("100"), List.of(), Map.of()));
    }

    @Test void exactSplitRejectsWrongTotal() {
        assertThrows(RuntimeException.class, () ->
                new ExactSplitStrategy().calculate(new BigDecimal("100"), members(),
                        Map.of(1, new BigDecimal("50"), 2, new BigDecimal("20"), 3, new BigDecimal("20"))));
    }

    @Test void exactSplitPreservesProvidedAmounts() {
        var r = new ExactSplitStrategy().calculate(new BigDecimal("100"), members(),
                Map.of(1, new BigDecimal("50"), 2, new BigDecimal("30"), 3, new BigDecimal("20")));
        assertEquals(new BigDecimal("50.00"), r.get(1));
        assertEquals(new BigDecimal("30.00"), r.get(2));
        assertEquals(new BigDecimal("20.00"), r.get(3));
    }

    @Test void percentageSplitCalculatesCorrectly() {
        var r = new PercentageSplitStrategy().calculate(new BigDecimal("200"), members(),
                Map.of(1, new BigDecimal("50"), 2, new BigDecimal("30"), 3, new BigDecimal("20")));
        assertEquals(new BigDecimal("100.00"), r.get(1));
        assertEquals(new BigDecimal("60.00"), r.get(2));
        assertEquals(new BigDecimal("40.00"), r.get(3));
    }

    @Test void percentageSplitRejectsNon100() {
        assertThrows(RuntimeException.class, () ->
                new PercentageSplitStrategy().calculate(new BigDecimal("200"), members(),
                        Map.of(1, new BigDecimal("50"), 2, new BigDecimal("30"), 3, new BigDecimal("10"))));
    }

    @Test void exactSplitRejectsNegativeAmount() {
        assertThrows(RuntimeException.class, () ->
                new ExactSplitStrategy().calculate(new BigDecimal("100"), members(),
                        Map.of(1, new BigDecimal("120"), 2, new BigDecimal("-10"), 3, new BigDecimal("-10"))));
    }

    @Test void percentageSplitTinyAmountNeverCreatesNegativeRemainder() {
        var r = new PercentageSplitStrategy().calculate(new BigDecimal("0.05"), members(),
                Map.of(1, new BigDecimal("50"), 2, new BigDecimal("50"), 3, new BigDecimal("0")));
        assertEquals(new BigDecimal("0.05"), r.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        assertTrue(r.values().stream().allMatch(value -> value.signum() >= 0));
    }

    @Test void percentageSplitRoundingRemainderPreservesTotal() {
        var r = new PercentageSplitStrategy().calculate(new BigDecimal("100.00"), members(),
                Map.of(1, new BigDecimal("33.33"), 2, new BigDecimal("33.33"), 3, new BigDecimal("33.34")));
        assertEquals(new BigDecimal("100.00"), r.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
