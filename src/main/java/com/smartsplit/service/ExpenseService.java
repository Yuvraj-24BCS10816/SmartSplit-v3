package com.smartsplit.service;

import com.smartsplit.dao.ExpenseDAO;
import com.smartsplit.exception.InvalidSplitException;
import com.smartsplit.model.Expense;
import com.smartsplit.model.Member;
import com.smartsplit.strategy.EqualSplitStrategy;
import com.smartsplit.strategy.ExactSplitStrategy;
import com.smartsplit.strategy.PercentageSplitStrategy;
import com.smartsplit.strategy.SplitStrategy;
import com.smartsplit.util.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Validates expense use cases and delegates allocations to the selected strategy. */
public class ExpenseService {
    private final ExpenseDAO dao = new ExpenseDAO();

    public int addExpense(int groupId,
                          String description,
                          String amountText,
                          int paidBy,
                          int categoryId,
                          LocalDate date,
                          String splitType,
                          List<Member> members,
                          Map<Integer, BigDecimal> values) {
        BigDecimal amount = Validator.positiveAmount(amountText);
        String cleanDescription = Validator.required(description, "Description");
        if (date == null) throw new IllegalArgumentException("Expense date is required.");
        if (splitType == null) throw new InvalidSplitException("Choose a split method.");

        SplitStrategy strategy = switch (splitType) {
            case "EQUAL" -> new EqualSplitStrategy();
            case "EXACT" -> new ExactSplitStrategy();
            case "PERCENTAGE" -> new PercentageSplitStrategy();
            default -> throw new InvalidSplitException("Unknown split method.");
        };

        Map<Integer, BigDecimal> shares = strategy.calculate(amount, members, values);
        Map<Integer, BigDecimal> percentages = strategy.percentages();
        Expense expense = new Expense(0, groupId, cleanDescription, amount, paidBy, categoryId,
                strategy.getName(), date);
        return dao.save(expense, shares, percentages);
    }

    public void delete(int expenseId) {
        dao.delete(expenseId);
    }

    public List<Expense> list(int groupId) {
        return dao.findByGroup(groupId);
    }

    public BigDecimal totalSpent(int groupId) {
        return dao.totalSpent(groupId);
    }

    public Map<Integer, BigDecimal> paid(int groupId) {
        return dao.totalPaidByUser(groupId);
    }

    public Map<Integer, BigDecimal> owed(int groupId) {
        return dao.totalSharesByUser(groupId);
    }
}
