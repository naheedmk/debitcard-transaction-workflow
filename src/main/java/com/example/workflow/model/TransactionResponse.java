package com.example.workflow.model;

import java.time.Instant;

public class TransactionResponse {
    private String transactionId;
    private String workflowId;
    private String status;
    private String message;
    private TransactionDetails details;

    public TransactionResponse() {
    }

    public TransactionResponse(String transactionId, String workflowId, String status, String message, TransactionDetails details) {
        this.transactionId = transactionId;
        this.workflowId = workflowId;
        this.status = status;
        this.message = message;
        this.details = details;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TransactionDetails getDetails() {
        return details;
    }

    public void setDetails(TransactionDetails details) {
        this.details = details;
    }
} 