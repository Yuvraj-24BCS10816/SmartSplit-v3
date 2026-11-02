package com.smartsplit.model;

import java.math.BigDecimal;

/** Stores one participant's share of an expense. */
public class ExpenseSplit {
    private int id;
    private int expenseId;
    private int userId;
    private BigDecimal shareAmount;
    private BigDecimal percentage;

    public ExpenseSplit() { }

    public ExpenseSplit(int id, int expenseId, int userId,
                        BigDecimal shareAmount, BigDecimal percentage) {
        this.id = id;
        this.expenseId = expenseId;
        this.userId = userId;
        this.shareAmount = shareAmount;
        this.percentage = percentage;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public BigDecimal getShareAmount() { return shareAmount; }
    public void setShareAmount(BigDecimal shareAmount) { this.shareAmount = shareAmount; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
