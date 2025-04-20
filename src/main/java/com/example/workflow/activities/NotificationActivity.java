package com.example.workflow.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.math.BigDecimal;

@ActivityInterface
public interface NotificationActivity {

    /**
     * Sends a notification to the customer.
     * @param customerId Identifier for the customer.
     * @param message The message content.
     */
    @ActivityMethod
    void sendNotification(String customerId, String message);

} 