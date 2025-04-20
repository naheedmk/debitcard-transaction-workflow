package com.example.workflow;

import com.example.workflow.activities.*;
import com.example.workflow.model.TransactionDetails;
import com.example.workflow.workflow.TransactionWorkflow;
import com.example.workflow.workflow.TransactionWorkflowImpl;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionWorkflowTest {
    private TestWorkflowEnvironment testEnv;
    private Worker worker;
    private TransactionWorkflow workflow;

    @BeforeEach
    void setUp() {
        // Create test environment
        testEnv = TestWorkflowEnvironment.newInstance();
        
        // Create worker
        worker = testEnv.newWorker(Shared.TASK_QUEUE_NAME);
        
        // Register workflow implementation
        worker.registerWorkflowImplementationTypes(TransactionWorkflowImpl.class);
        
        // Register activity implementations
        worker.registerActivitiesImplementations(
            new AccountActivityImpl(),
            new DebitCardActivityImpl(),
            new FraudCheckActivityImpl(),
            new NotificationActivityImpl()
        );
        
        // Start test environment
        testEnv.start();
        
        // Create workflow stub
        workflow = testEnv.getWorkflowClient().newWorkflowStub(
            TransactionWorkflow.class,
            io.temporal.client.WorkflowOptions.newBuilder()
                .setTaskQueue(Shared.TASK_QUEUE_NAME)
                .build()
        );
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void testSuccessfulTransaction() {
        // Create transaction details
        TransactionDetails details = new TransactionDetails(
            "test-txn-001",
            "account-123", // This account has 1000.00 balance
            "card-123",
            new BigDecimal("100.00"),
            "customer-123"
        );

        // Execute workflow
        String result = workflow.processTransaction(details);

        // Verify result
        assertEquals("Completed Successfully", result);
    }

    @Test
    void testInsufficientFunds() {
        // Create transaction details with amount exceeding balance
        TransactionDetails details = new TransactionDetails(
            "test-txn-002",
            "account-456", // This account has 50.00 balance
            "card-456",
            new BigDecimal("100.00"),
            "customer-456"
        );

        // Execute workflow
        String result = workflow.processTransaction(details);

        // Verify result
        assertEquals("Failed: Insufficient Funds or Limit Exceeded", result);
    }

    @Test
    void testFraudulentTransaction() {
        // Create transaction details
        TransactionDetails details = new TransactionDetails(
            "test-txn-003",
            "account-123",
            "card-123",
            new BigDecimal("500.00"), // Large amount that might trigger fraud check
            "customer-123"
        );

        // Execute workflow
        String result = workflow.processTransaction(details);

        // Verify result - could be either fraud or success due to randomness
        assertTrue(result.equals("Failed: Fraud Detected") || result.equals("Completed Successfully"));
    }

    @Test
    void testMissingRequiredFields() {
        // Create transaction details with missing fields
        TransactionDetails details = new TransactionDetails();
        details.setTransactionId("test-txn-004");
        // Missing accountId, cardId, and amount

        // Execute workflow
        String result = workflow.processTransaction(details);

        // Verify result
        assertEquals("Failed: Missing required fields", result);
    }

    @Test
    void testLargeTransaction() {
        // Create transaction details with large amount
        TransactionDetails details = new TransactionDetails(
            "test-txn-005",
            "account-123",
            "card-123",
            new BigDecimal("2000.00"), // Large amount
            "customer-123"
        );

        // Execute workflow
        String result = workflow.processTransaction(details);

        // Verify result - could be either insufficient funds or success due to randomness
        assertTrue(result.equals("Failed: Insufficient Funds or Limit Exceeded") || 
                  result.equals("Completed Successfully"));
    }
} 