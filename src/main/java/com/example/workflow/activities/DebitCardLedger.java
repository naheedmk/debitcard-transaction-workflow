package com.example.workflow.activities;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Simulated Database/Ledger for Debit Card System
public class DebitCardLedger {
    // Represents the preset limit or available funds in the debit card system itself
    private static final Map<String, BigDecimal> limits = new HashMap<>();
    private static final Map<String, Map<String, BigDecimal>> memoHolds = new HashMap<>(); // cardId -> {transactionId -> amount}

    static {
        // Initialize with some dummy data
        limits.put("card-123", new BigDecimal("5000.00")); // Higher limit
        limits.put("card-456", new BigDecimal("200.00")); // Lower limit
        System.out.println("DebitCardLedger initialized with dummy data.");
    }

    public static synchronized boolean isWithinLimit(String cardId, BigDecimal amount) {
        BigDecimal cardLimit = limits.getOrDefault(cardId, BigDecimal.ZERO);
        BigDecimal totalHolds = memoHolds.getOrDefault(cardId, new HashMap<>()).values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean sufficient = cardLimit.subtract(totalHolds).compareTo(amount) >= 0;
         // System.out.println(String.format("Checking limit for %s: Limit=%.2f, Holds=%.2f, Request=%.2f, Sufficient=%b",
         //        cardId, cardLimit, totalHolds, amount, sufficient));
        return sufficient;
    }

    public static synchronized void addMemoHold(String cardId, String transactionId, BigDecimal amount) {
        memoHolds.computeIfAbsent(cardId, k -> new HashMap<>()).put(transactionId, amount);
        // System.out.println(String.format("Added memo hold for %s, Txn=%s, Amount=%.2f. Holds: %s",
        //         cardId, transactionId, amount, getCurrentMemoHolds(cardId)));
    }

    public static synchronized void removeMemoHold(String cardId, String transactionId) {
        if (memoHolds.containsKey(cardId)) {
            memoHolds.get(cardId).remove(transactionId);
            if (memoHolds.get(cardId).isEmpty()) {
                memoHolds.remove(cardId);
            }
           // System.out.println(String.format("Removed memo hold for %s, Txn=%s. Holds: %s",
           //      cardId, transactionId, getCurrentMemoHolds(cardId)));
        }
    }

    // In a real system, this might not actually change a "limit", but rather
    // confirm the usage against the limit. Here we just remove the hold.
    public static synchronized void applyFinalPost(String cardId, String transactionId, BigDecimal amount) {
        removeMemoHold(cardId, transactionId);
        // System.out.println(String.format("Applied final post for card %s, Txn=%s, Amount=%.2f. Holds removed.",
        //         cardId, transactionId, amount));
    }

     public static synchronized BigDecimal getLimit(String cardId) {
        return limits.getOrDefault(cardId, BigDecimal.ZERO);
     }

      public static synchronized Map<String, BigDecimal> getCurrentMemoHolds(String cardId) {
         return Collections.unmodifiableMap(memoHolds.getOrDefault(cardId, Collections.emptyMap()));
     }
} 