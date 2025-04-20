package com.example.workflow.activities;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
// Removed imports for HashMap, Map, Collections as they are now encapsulated in CheckingAccountLedger

// Simulated Database/Ledger for Checking Accounts - REMOVED FROM HERE

public class AccountActivityImpl implements AccountActivity {

    private static final Logger log = LoggerFactory.getLogger(AccountActivityImpl.class);

    @Override
    public boolean checkBalance(String accountId, BigDecimal amount) {
        log.info("Checking balance for account {} for amount {}", accountId, amount);
        // Simulate network delay
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        // Now calls the static method on the separate class
        boolean sufficient = CheckingAccountLedger.hasSufficientFunds(accountId, amount);
        log.info("Account {} balance sufficient for amount {}: {}", accountId, amount, sufficient);
        return sufficient;
    }

    @Override
    public void memoPost(String accountId, String transactionId, BigDecimal amount, String type) {
        log.info("Performing {} memo post on account {} for transaction {} amount {}", type, accountId, transactionId, amount);
        // Simulate network delay
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if ("debit".equalsIgnoreCase(type)) {
             CheckingAccountLedger.addMemoPost(accountId, transactionId, amount);
             log.info("Memo post DEBIT successful for account {}, transaction {}, amount {}. Current Balance: {}, Memo Held: {}",
                 accountId, transactionId, amount, CheckingAccountLedger.getBalance(accountId), CheckingAccountLedger.getCurrentMemoPosts(accountId)); // Use getter
        } else {
             // If reversing, we just ensure the memo is removed
             CheckingAccountLedger.removeMemoPost(accountId, transactionId);
             log.info("Memo post CREDIT (reversal) successful for account {}, transaction {}, amount {}. Current Balance: {}, Memo Held: {}",
                 accountId, transactionId, amount, CheckingAccountLedger.getBalance(accountId), CheckingAccountLedger.getCurrentMemoPosts(accountId)); // Use getter
        }
    }

    @Override
    public void finalPost(String accountId, String transactionId, BigDecimal amount) {
        log.info("Performing final post on account {} for transaction {} amount {}", accountId, transactionId, amount);
        // Simulate network delay
        try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        CheckingAccountLedger.applyFinalPost(accountId, transactionId, amount);
        log.info("Final post successful for account {}, transaction {}. New Balance: {}", accountId, transactionId, CheckingAccountLedger.getBalance(accountId));
    }

    @Override
    public void reverseMemoPost(String accountId, String transactionId, BigDecimal amount) {
        log.info("Reversing memo post on account {} for transaction {} amount {}", accountId, transactionId, amount);
        // This is essentially a credit memo post in this simplified model
        memoPost(accountId, transactionId, amount, "credit");
    }
} 