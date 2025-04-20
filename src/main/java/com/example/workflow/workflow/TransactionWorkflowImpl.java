package com.example.workflow.workflow;

import com.example.workflow.Shared;
import com.example.workflow.activities.*;
import com.example.workflow.model.TransactionDetails;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class TransactionWorkflowImpl implements TransactionWorkflow {

    private final Logger log = Workflow.getLogger(TransactionWorkflowImpl.class);

    // Define activity options with timeouts and retries
    // Activity Execution options specify how Activities are invoked
    private final ActivityOptions options = ActivityOptions.newBuilder()
            .setTaskQueue(Shared.TASK_QUEUE_NAME)
            .setStartToCloseTimeout(Duration.ofSeconds(10)) // Max time for one activity execution
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1)) // First retry after 1s
                    .setMaximumAttempts(3) // Retry up to 3 times
                    .build())
            .build();

    // Activity Stubs: Proxies to the actual activity implementations
    private final AccountActivity accountActivity = Workflow.newActivityStub(AccountActivity.class, options);
    private final DebitCardActivity debitCardActivity = Workflow.newActivityStub(DebitCardActivity.class, 
        ActivityOptions.newBuilder()
            .setTaskQueue(Shared.TASK_QUEUE_NAME)
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setScheduleToCloseTimeout(Duration.ofSeconds(30))
            .setRetryOptions(RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setMaximumAttempts(3)
                .build())
            .build());
    // Use slightly longer timeout for potentially long-running fraud check
    private final FraudCheckActivity fraudCheckActivity = Workflow.newActivityStub(FraudCheckActivity.class, 
        ActivityOptions.newBuilder(options).setStartToCloseTimeout(Duration.ofSeconds(30)).build());
    private final NotificationActivity notificationActivity = Workflow.newActivityStub(NotificationActivity.class, options);

    @Override
    public String processTransaction(TransactionDetails details) {
        log.info("Processing transaction: {}", details);
        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(true).build());
        String finalStatus = "Unknown Error"; // Default status

        try {
            // Validate required fields
            if (details.getTransactionId() == null || details.getAccountId() == null || 
                details.getCardId() == null || details.getAmount() == null) {
                finalStatus = "Failed: Missing required fields";
                log.error("Transaction {} failed: Missing required fields", details.getTransactionId());
                return finalStatus;
            }

            // 1. Parallel Validations
            log.info("Step 1: Starting parallel validations (Account Balance & Card Limit)");
            Promise<Boolean> accountBalanceCheck = Async.function(() -> 
                accountActivity.checkBalance(details.getAccountId(), details.getAmount()));
            Promise<Boolean> cardLimitCheck = Async.function(() -> 
                debitCardActivity.checkLimit(details.getCardId(), details.getAmount()));

            // Wait for both validations to complete
            boolean accountOk = accountBalanceCheck.get();
            boolean cardOk = cardLimitCheck.get();
            log.info("Validation Results: Account OK = {}, Card OK = {}", accountOk, cardOk);

            if (!accountOk || !cardOk) {
                finalStatus = "Failed: Insufficient Funds or Limit Exceeded";
                log.warn("Transaction {} failed validation.", details.getTransactionId());
                // No compensation needed as nothing has been posted yet
                return finalStatus;
            }

            // 2. Parallel Memo Posts + Async Fraud Check
            log.info("Step 2: Starting parallel Memo Posts and async Fraud Check");
            // Add compensation actions to Saga *before* executing the actions
            saga.addCompensation(() -> 
                accountActivity.reverseMemoPost(details.getAccountId(), details.getTransactionId(), details.getAmount()));
            saga.addCompensation(() -> 
                debitCardActivity.reverseMemoPost(details.getCardId(), details.getTransactionId(), details.getAmount()));

            // Execute Memo Posts in parallel
            Promise<Void> accountMemoPost = Async.procedure(() -> 
                accountActivity.memoPost(details.getAccountId(), details.getTransactionId(), details.getAmount(), "debit"));
            Promise<Void> cardMemoPost = Async.procedure(() -> 
                debitCardActivity.memoPost(details.getCardId(), details.getTransactionId(), details.getAmount(), "debit"));
            
            // Start fraud check asynchronously
            Promise<Boolean> fraudCheckResult = Async.function(() -> 
                fraudCheckActivity.isFraudulent(details.getTransactionId(), details.getAccountId(), 
                    details.getCardId(), details.getAmount()));

            // Wait for memo posts to complete first (usually faster)
            accountMemoPost.get();
            cardMemoPost.get();
            log.info("Memo posts completed for transaction {}.", details.getTransactionId());

            // Now wait for the potentially longer fraud check
            log.info("Waiting for fraud check result...");
            boolean isFraud = fraudCheckResult.get();
            log.info("Fraud check result: {}", isFraud);

            // 3. Handle Fraud Result
            if (isFraud) {
                finalStatus = "Failed: Fraud Detected";
                log.warn("FRAUD DETECTED for transaction {}. Initiating compensation.", details.getTransactionId());
                // Compensate: This will execute the reverseMemoPost actions added earlier
                saga.compensate(); 
                log.info("Compensation completed for transaction {}.", details.getTransactionId());
                // Optionally send a fraud alert notification
                if (details.getCustomerId() != null) {
                    notificationActivity.sendNotification(details.getCustomerId(), 
                        String.format("Fraud Alert: Transaction %s for %.2f was flagged and reversed.", details.getTransactionId(), details.getAmount()));
                }
                return finalStatus;
            }

            // 4. Final Posting (if not fraud)
            log.info("Step 4: No fraud detected. Performing final posts.");
            // Note: We don't typically need parallel final posts, but could do if needed.
            // For simplicity, doing sequentially. These actions *remove* the memo holds/posts.
            accountActivity.finalPost(details.getAccountId(), details.getTransactionId(), details.getAmount());
            debitCardActivity.finalPost(details.getCardId(), details.getTransactionId(), details.getAmount());
            log.info("Final posts completed for transaction {}.", details.getTransactionId());

            // 5. Send Notification (Success)
            log.info("Step 5: Sending success notification.");
            if (details.getCustomerId() != null) {
                notificationActivity.sendNotification(details.getCustomerId(), 
                    String.format("Transaction %s for %.2f completed successfully.", details.getTransactionId(), details.getAmount()));
            }
            
            finalStatus = "Completed Successfully";
            log.info("Workflow completed for transaction {}: {}", details.getTransactionId(), finalStatus);
            
            // If we reached here successfully, clear the saga compensation actions
            // (alternative to compensate is cleanup which runs compensation regardless)
            // saga.cleanup(); // Use cleanup if you want compensation to run even on success (e.g., audit logs)

        } catch (ActivityFailure e) {
            log.error("Activity failed during transaction {} processing: {}. Initiating compensation.", 
                      details.getTransactionId(), e.getMessage(), e);
            finalStatus = "Failed: Activity Error - Check Logs";
            saga.compensate(); // Attempt to reverse any completed steps
        } catch (Exception e) {
            log.error("Workflow failed unexpectedly during transaction {} processing: {}. Initiating compensation.", 
                      details.getTransactionId(), e.getMessage(), e);
            finalStatus = "Failed: Workflow Error - Check Logs";
            saga.compensate(); // Attempt to reverse any completed steps
        }

        return finalStatus;
    }
} 