package com.example.workflow.activities;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

// Simulated Database/Ledger for Debit Card System

public class DebitCardActivityImpl implements DebitCardActivity {

    private static final Logger log = LoggerFactory.getLogger(DebitCardActivityImpl.class);

    @Override
    public boolean checkLimit(String cardId, BigDecimal amount) {
        log.info("Checking limit for card {} for amount {}", cardId, amount);
        // Simulate network delay
        try { Thread.sleep(60); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        boolean sufficient = DebitCardLedger.isWithinLimit(cardId, amount);
        log.info("Card {} limit sufficient for amount {}: {}", cardId, amount, sufficient);
        return sufficient;
    }

    @Override
    public void memoPost(String cardId, String transactionId, BigDecimal amount, String type) {
        log.info("Performing {} memo post on card {} for transaction {} amount {}", type, cardId, transactionId, amount);
         // Simulate network delay
        try { Thread.sleep(110); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
       if ("debit".equalsIgnoreCase(type)) {
            DebitCardLedger.addMemoHold(cardId, transactionId, amount);
            log.info("Memo post DEBIT successful for card {}, transaction {}, amount {}. Current Limit: {}, Holds: {}",
                cardId, transactionId, amount, DebitCardLedger.getLimit(cardId), DebitCardLedger.getCurrentMemoHolds(cardId));
       } else {
            // Reversal removes the hold
            DebitCardLedger.removeMemoHold(cardId, transactionId);
             log.info("Memo post CREDIT (reversal) successful for card {}, transaction {}, amount {}. Current Limit: {}, Holds: {}",
                cardId, transactionId, amount, DebitCardLedger.getLimit(cardId), DebitCardLedger.getCurrentMemoHolds(cardId));
       }
    }

    @Override
    public void finalPost(String cardId, String transactionId, BigDecimal amount) {
        log.info("Performing final post on card {} for transaction {} amount {}", cardId, transactionId, amount);
         // Simulate network delay
        try { Thread.sleep(160); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        DebitCardLedger.applyFinalPost(cardId, transactionId, amount);
        log.info("Final post successful for card {}, transaction {}. Holds: {}", cardId, transactionId, DebitCardLedger.getCurrentMemoHolds(cardId));
    }

    @Override
    public void reverseMemoPost(String cardId, String transactionId, BigDecimal amount) {
        log.info("Reversing memo post on card {} for transaction {} amount {}", cardId, transactionId, amount);
        // Reversal removes the hold
        memoPost(cardId, transactionId, amount, "credit");
    }
} 