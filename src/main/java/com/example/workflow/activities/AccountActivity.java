package com.example.workflow.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.math.BigDecimal;

@ActivityInterface
public interface AccountActivity {

    @ActivityMethod(name = "AccountCheckBalance")
    boolean checkBalance(String accountId, BigDecimal amount);

    @ActivityMethod(name = "AccountMemoPost")
    void memoPost(String accountId, String transactionId, BigDecimal amount, String type);

    @ActivityMethod(name = "AccountFinalPost")
    void finalPost(String accountId, String transactionId, BigDecimal amount);

    @ActivityMethod(name = "AccountReverseMemoPost")
    void reverseMemoPost(String accountId, String transactionId, BigDecimal amount);

} 