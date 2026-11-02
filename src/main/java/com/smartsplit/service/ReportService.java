package com.smartsplit.service;

import com.smartsplit.dao.ExpenseDAO;
import com.smartsplit.dao.GroupDAO;
import com.smartsplit.model.Expense;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Produces group-level reporting metrics from persisted records. */
public class ReportService {
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final GroupDAO groupDAO = new GroupDAO();

    public ReportData summary(int groupId) {
        List<Expense> expenses = expenseDAO.findByGroup(groupId);
        BigDecimal total = expenseDAO.totalSpent(groupId);
        BigDecimal average = expenses.isEmpty()
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : total.divide(BigDecimal.valueOf(expenses.size()), 2, RoundingMode.HALF_UP);
        BigDecimal largest = expenses.stream()
                .map(Expense::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return new ReportData(total, expenses.size(), average, largest, groupDAO.members(groupId).size());
    }

    public record ReportData(BigDecimal total,
                             int expenseCount,
                             BigDecimal average,
                             BigDecimal largest,
                             int memberCount) { }
}
