package com.example.workflow.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.math.BigDecimal;

@ActivityInterface
public interface DebitCardActivity {

    @ActivityMethod(name = "DebitCardCheckLimit")
    boolean checkLimit(String cardId, BigDecimal amount);

    @ActivityMethod(name = "DebitCardMemoPost")
    void memoPost(String cardId, String transactionId, BigDecimal amount, String type);

    @ActivityMethod(name = "DebitCardFinalPost")
    void finalPost(String cardId, String transactionId, BigDecimal amount);

    @ActivityMethod(name = "DebitCardReverseMemoPost")
    void reverseMemoPost(String cardId, String transactionId, BigDecimal amount);

} 