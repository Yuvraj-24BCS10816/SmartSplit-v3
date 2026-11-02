package com.smartsplit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A suggested or completed payment between two group members. */
public class Settlement {
    private int id;
    private int groupId;
    private int payerId;
    private int receiverId;
    private BigDecimal amount;
    private LocalDate date;
    private String status;
    private String payerName;
    private String receiverName;

    public Settlement() { }

    public Settlement(int id, int groupId, int payerId, int receiverId,
                      BigDecimal amount, LocalDate date, String status) {
        this.id = id;
        this.groupId = groupId;
        this.payerId = payerId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public int getPayerId() { return payerId; }
    public void setPayerId(int payerId) { this.payerId = payerId; }
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    @Override
    public String toString() {
        return payerName + " → " + receiverName + " : ₹" + amount + " (" + status + ")";
    }
}
