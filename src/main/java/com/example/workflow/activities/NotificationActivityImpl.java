package com.example.workflow.activities;

import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationActivityImpl implements NotificationActivity {

    private static final Logger log = LoggerFactory.getLogger(NotificationActivityImpl.class);

    @Override
    public void sendNotification(String customerId, String message) {
        log.info("Sending notification to customer {}: '{}'", customerId, message);
        // Simulate sending notification (e.g., email, SMS)
        try {
            Thread.sleep(50); // Simulate quick operation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Notification sent successfully to customer {}.");
    }
} 