package com.example.workflow.activity;

import com.example.workflow.model.TransactionDetails;
import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebitCardActivityImpl implements DebitCardActivity {
    private static final Logger logger = LoggerFactory.getLogger(DebitCardActivityImpl.class);

    @Override
    public void checkLimit(TransactionDetails details) {
        logger.info("Checking limit for transaction: {}", details.getTransactionId());
        // Simulate limit check
        if (details.getAmount().compareTo(java.math.BigDecimal.valueOf(1000)) > 0) {
            throw Activity.wrap(new RuntimeException("Transaction amount exceeds limit"));
        }
    }

    @Override
    public void memoPost(TransactionDetails details) {
        logger.info("Performing memo post for transaction: {}", details.getTransactionId());
        // Simulate memo post
        try {
            Thread.sleep(1000); // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Activity.wrap(e);
        }
    }

    @Override
    public void finalPost(TransactionDetails details) {
        logger.info("Performing final post for transaction: {}", details.getTransactionId());
        // Simulate final post
        try {
            Thread.sleep(1000); // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Activity.wrap(e);
        }
    }
} 