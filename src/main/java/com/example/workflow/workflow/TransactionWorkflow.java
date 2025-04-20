package com.example.workflow.workflow;

import com.example.workflow.model.TransactionDetails;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TransactionWorkflow {

    /**
     * Main entry point for the debit card transaction workflow.
     * @param details The details of the transaction.
     * @return A status message (e.g., "Completed", "Failed: Insufficient Funds", "Failed: Fraud Detected").
     */
    @WorkflowMethod
    String processTransaction(TransactionDetails details);

} 