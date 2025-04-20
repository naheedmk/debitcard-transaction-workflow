package com.example.workflow.activities;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// Simulated Database/Ledger for Checking Accounts
public class CheckingAccountLedger {
    private static final Map<String, BigDecimal> balances = new HashMap<>();
    private static final Map<String, Map<String, BigDecimal>> memoPosts = new HashMap<>(); // accountId -> {transactionId -> amount}
    private static final Random random = new Random(); // For randomizing balance checks

    static {
        // Initialize with some dummy data
        balances.put("account-123", new BigDecimal("1000.00"));
        balances.put("account-456", new BigDecimal("50.00"));
        System.out.println("CheckingAccountLedger initialized with dummy data."); // Added for visibility
    }

    // Make methods public and static
    public static synchronized boolean hasSufficientFunds(String accountId, BigDecimal amount) {
        BigDecimal currentBalance = balances.getOrDefault(accountId, BigDecimal.ZERO);
        BigDecimal totalMemos = memoPosts.getOrDefault(accountId, new HashMap<>()).values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return currentBalance.subtract(totalMemos).compareTo(amount) >= 0;
    }

    public static synchronized void addMemoPost(String accountId, String transactionId, BigDecimal amount) {
        memoPosts.computeIfAbsent(accountId, k -> new HashMap<>()).put(transactionId, amount);
        // System.out.println(String.format("Added memo for %s, Txn=%s, Amount=%.2f. Memos: %s", 
        //         accountId, transactionId, amount, getCurrentMemoPosts(accountId)));
    }

    public static synchronized void removeMemoPost(String accountId, String transactionId) {
        if (memoPosts.containsKey(accountId)) {
            memoPosts.get(accountId).remove(transactionId);
            if (memoPosts.get(accountId).isEmpty()) {
                memoPosts.remove(accountId);
            }
           // System.out.println(String.format("Removed memo for %s, Txn=%s. Memos: %s", 
           //      accountId, transactionId, getCurrentMemoPosts(accountId)));
        }
    }

    public static synchronized void applyFinalPost(String accountId, String transactionId, BigDecimal amount) {
        removeMemoPost(accountId, transactionId); // Remove memo first
        BigDecimal currentBalance = balances.getOrDefault(accountId, BigDecimal.ZERO);
        balances.put(accountId, currentBalance.subtract(amount));
        // System.out.println(String.format("Applied final post for %s, Txn=%s, Amount=%.2f. New Balance: %.2f",
        //         accountId, transactionId, amount, getBalance(accountId)));
    }

     public static synchronized BigDecimal getBalance(String accountId) {
        return balances.getOrDefault(accountId, BigDecimal.ZERO);
     }

     // Getter for memo posts for logging/inspection
     public static synchronized Map<String, BigDecimal> getCurrentMemoPosts(String accountId) {
         return Collections.unmodifiableMap(memoPosts.getOrDefault(accountId, Collections.emptyMap()));
     }
} 