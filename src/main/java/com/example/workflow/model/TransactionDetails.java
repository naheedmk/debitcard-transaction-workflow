package com.example.workflow.model;

import java.math.BigDecimal;

// Simple Plain Old Java Object (POJO) for transaction details
public class TransactionDetails {
    private String transactionId;
    private String accountId;
    private String cardId;
    private BigDecimal amount;
    private String customerId; // Needed for notification

    // Default constructor (needed for Jackson/Temporal serialization)
    public TransactionDetails() {}

    public TransactionDetails(String transactionId, String accountId, String cardId, BigDecimal amount, String customerId) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.cardId = cardId;
        this.amount = amount;
        this.customerId = customerId;
    }

    // Getters (needed for Jackson/Temporal serialization)
    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCardId() {
        return cardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    // Optional: Setters can be added if needed, but often immutable is preferred for inputs
    // Optional: toString for logging
    @Override
    public String toString() {
        return "TransactionDetails{" +
                "transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", cardId='" + cardId + '\'' +
                ", amount=" + amount +
                ", customerId='" + customerId + '\'' +
                '}';
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
} 