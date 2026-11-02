package com.smartsplit.service;

import com.smartsplit.dao.ExpenseDAO;
import com.smartsplit.dao.GroupDAO;
import com.smartsplit.dao.SettlementDAO;
import com.smartsplit.model.Balance;
import com.smartsplit.model.Member;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Rebuilds group net balances from expense and settlement history. */
public class BalanceService {
    private final ExpenseDAO expenses = new ExpenseDAO();
    private final SettlementDAO settlements = new SettlementDAO();
    private final GroupDAO groups = new GroupDAO();

    /**
     * A positive value means money should be received; a negative value means money is owed.
     * Completed settlements move the payer toward zero and the receiver toward zero.
     */
    public Map<Integer, BigDecimal> netBalances(int groupId) {
        Map<Integer, BigDecimal> paid = expenses.totalPaidByUser(groupId);
        Map<Integer, BigDecimal> owed = expenses.totalSharesByUser(groupId);
        Map<Integer, BigDecimal> settlementEffects = settlements.netCompletedSettlements(groupId);
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();

        for (Member member : groups.members(groupId)) {
            BigDecimal totalPaid = paid.getOrDefault(member.getId(), BigDecimal.ZERO);
            BigDecimal totalOwed = owed.getOrDefault(member.getId(), BigDecimal.ZERO);
            BigDecimal settlementEffect = settlementEffects.getOrDefault(member.getId(), BigDecimal.ZERO);
            BigDecimal net = totalPaid.subtract(totalOwed).add(settlementEffect)
                    .setScale(2, RoundingMode.HALF_UP);
            result.put(member.getId(), net);
        }
        return result;
    }

    public List<Balance> balances(int groupId) {
        Map<Integer, BigDecimal> net = netBalances(groupId);
        List<Balance> result = new ArrayList<>();
        for (Member member : groups.members(groupId)) {
            result.add(new Balance(member, net.getOrDefault(member.getId(), BigDecimal.ZERO)));
        }
        return result;
    }
}
