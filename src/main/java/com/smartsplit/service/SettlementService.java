package com.smartsplit.service;

import com.smartsplit.dao.GroupDAO;
import com.smartsplit.dao.SettlementDAO;
import com.smartsplit.exception.SettlementException;
import com.smartsplit.model.Member;
import com.smartsplit.model.Settlement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Creates settlement suggestions and manages their persisted status. */
public class SettlementService {
    private final BalanceService balanceService = new BalanceService();
    private final SettlementDAO dao = new SettlementDAO();
    private final GroupDAO groupDAO = new GroupDAO();

    /**
     * Produces a greedy debtor-to-creditor payment plan from current net balances.
     * The suggestions are calculations only; they are not saved until the user chooses to save them.
     */
    public List<Settlement> suggestions(int groupId) {
        Map<Integer, BigDecimal> netBalances = balanceService.netBalances(groupId);
        Map<Integer, String> memberNames = new HashMap<>();
        for (Member member : groupDAO.members(groupId)) {
            memberNames.put(member.getId(), member.getName());
        }

        List<Position> creditors = new ArrayList<>();
        List<Position> debtors = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : netBalances.entrySet()) {
            BigDecimal amount = entry.getValue();
            if (amount.signum() > 0) {
                creditors.add(new Position(entry.getKey(), amount));
            } else if (amount.signum() < 0) {
                debtors.add(new Position(entry.getKey(), amount.abs()));
            }
        }

        List<Settlement> result = new ArrayList<>();
        int debtorIndex = 0;
        int creditorIndex = 0;
        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            Position debtor = debtors.get(debtorIndex);
            Position creditor = creditors.get(creditorIndex);
            BigDecimal amount = debtor.amount.min(creditor.amount).setScale(2, RoundingMode.HALF_UP);

            Settlement suggestion = new Settlement(
                    0, groupId, debtor.userId, creditor.userId, amount, LocalDate.now(), "PENDING");
            suggestion.setPayerName(memberNames.get(debtor.userId));
            suggestion.setReceiverName(memberNames.get(creditor.userId));
            result.add(suggestion);

            debtor.amount = debtor.amount.subtract(amount);
            creditor.amount = creditor.amount.subtract(amount);
            if (debtor.amount.signum() == 0) debtorIndex++;
            if (creditor.amount.signum() == 0) creditorIndex++;
        }
        return result;
    }

    public int save(Settlement settlement) {
        if (settlement.getAmount() == null || settlement.getAmount().signum() <= 0) {
            throw new SettlementException("Settlement amount must be positive.");
        }
        if (settlement.getPayerId() == settlement.getReceiverId()) {
            throw new SettlementException("Payer and receiver must be different.");
        }
        if (dao.hasPending(settlement.getGroupId(), settlement.getPayerId(),
                settlement.getReceiverId(), settlement.getAmount())) {
            throw new SettlementException("This settlement is already pending.");
        }
        return dao.save(settlement);
    }

    public void complete(int settlementId) {
        dao.complete(settlementId);
    }

    public List<Settlement> history(int groupId) {
        return dao.findByGroup(groupId);
    }

    private static final class Position {
        private final int userId;
        private BigDecimal amount;

        private Position(int userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }
    }
}
