package com.example.workflow.activities;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Random;

public class FraudCheckActivityImpl implements FraudCheckActivity {

    private static final Logger log = LoggerFactory.getLogger(FraudCheckActivityImpl.class);
    private static final Random random = new Random();

    @Override
    public boolean isFraudulent(String transactionId, String accountId, String cardId, BigDecimal amount) {
        log.info("Starting fraud check for transaction {}, account {}, card {}, amount {}", 
                 transactionId, accountId, cardId, amount);

        // Simulate a longer processing time for fraud check
        int sleepMillis = 1000 + random.nextInt(2000); // Simulate 1-3 seconds delay
        log.info("Simulating fraud check duration: {} ms", sleepMillis);
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            log.warn("Fraud check sleep interrupted for transaction {}", transactionId);
            Thread.currentThread().interrupt();
            // Decide how to handle interruption - maybe assume not fraud?
            // For simplicity, let's continue and still generate a random result
        }

        // Simulate random fraud result (e.g., 10% chance of fraud)
        boolean isFraud = random.nextInt(10) == 0; 

        log.info("Fraud check completed for transaction {}. Result: {}", transactionId, isFraud ? "FRAUD DETECTED" : "Not Fraud");
        return isFraud;
    }
} 