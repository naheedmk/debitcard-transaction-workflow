package com.example.workflow.activity;

import com.example.workflow.model.TransactionDetails;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DebitCardActivity {
    @ActivityMethod
    void checkLimit(TransactionDetails details);

    @ActivityMethod
    void memoPost(TransactionDetails details);

    @ActivityMethod
    void finalPost(TransactionDetails details);
} 