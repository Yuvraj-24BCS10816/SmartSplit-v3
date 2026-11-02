package com.smartsplit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Domain model for one group expense. */
public class Expense {
    private int id;
    private int groupId;
    private int paidBy;
    private int categoryId;
    private String description;
    private String splitType;
    private BigDecimal amount;
    private LocalDate expenseDate;

    public Expense() { }

    public Expense(int id, int groupId, String description, BigDecimal amount,
                   int paidBy, int categoryId, String splitType, LocalDate expenseDate) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.categoryId = categoryId;
        this.splitType = splitType;
        this.expenseDate = expenseDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public int getPaidBy() { return paidBy; }
    public void setPaidBy(int paidBy) { this.paidBy = paidBy; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
}
