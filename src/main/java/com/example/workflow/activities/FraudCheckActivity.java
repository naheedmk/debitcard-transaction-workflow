package com.example.workflow.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.math.BigDecimal;

@ActivityInterface
public interface FraudCheckActivity {

    /**
     * Performs a fraud check for the given transaction details.
     * @param transactionId Unique ID for the transaction.
     * @param accountId Account involved.
     * @param cardId Card involved.
     * @param amount Transaction amount.
     * @return true if the transaction is considered fraudulent, false otherwise.
     */
    @ActivityMethod(name = "IsFraudulent")
    boolean isFraudulent(String transactionId, String accountId, String cardId, BigDecimal amount);
} 