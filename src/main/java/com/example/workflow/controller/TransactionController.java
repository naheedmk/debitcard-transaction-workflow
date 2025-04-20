package com.example.workflow.controller;

import com.example.workflow.model.TransactionDetails;
import com.example.workflow.model.TransactionResponse;
import com.example.workflow.workflow.TransactionWorkflow;
import com.example.workflow.config.TemporalConfig;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final WorkflowClient workflowClient;

    @Autowired
    public TransactionController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> startTransaction(@RequestBody TransactionDetails details) {
        log.info("Received transaction request via API: {}", details);

        // Validate required fields
        if (details.getTransactionId() == null || details.getTransactionId().isEmpty()) {
            log.warn("Rejecting request: Missing transactionId");
            TransactionResponse response = new TransactionResponse();
            response.setStatus("ERROR");
            response.setMessage("Missing transactionId");
            response.setDetails(details);
            return ResponseEntity.badRequest().body(response);
        }

        if (details.getAccountId() == null || details.getAccountId().isEmpty()) {
            log.warn("Rejecting request: Missing accountId");
            TransactionResponse response = new TransactionResponse();
            response.setStatus("ERROR");
            response.setMessage("Missing accountId");
            response.setDetails(details);
            return ResponseEntity.badRequest().body(response);
        }

        if (details.getCardId() == null || details.getCardId().isEmpty()) {
            log.warn("Rejecting request: Missing cardId");
            TransactionResponse response = new TransactionResponse();
            response.setStatus("ERROR");
            response.setMessage("Missing cardId");
            response.setDetails(details);
            return ResponseEntity.badRequest().body(response);
        }

        if (details.getAmount() == null) {
            log.warn("Rejecting request: Missing amount");
            TransactionResponse response = new TransactionResponse();
            response.setStatus("ERROR");
            response.setMessage("Missing amount");
            response.setDetails(details);
            return ResponseEntity.badRequest().body(response);
        }

        // Generate a unique workflow ID by appending timestamp
        String workflowId = String.format("debit-card-txn-%s-%d", 
            details.getTransactionId(), 
            Instant.now().toEpochMilli()
        );

        WorkflowOptions wfOptions = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(TemporalConfig.TASK_QUEUE)
                .build();

        TransactionWorkflow workflow = workflowClient.newWorkflowStub(TransactionWorkflow.class, wfOptions);

        try {
            log.info("Starting workflow with ID: {} on task queue: {}", workflowId, TemporalConfig.TASK_QUEUE);
            WorkflowClient.start(workflow::processTransaction, details);
            log.info("Workflow {} started successfully via API request.", workflowId);

            // Create response with initial status
            TransactionResponse response = new TransactionResponse(
                details.getTransactionId(),
                workflowId,
                "STARTED",
                "Transaction workflow started successfully",
                details
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to start workflow {} via API: {}", workflowId, e.getMessage(), e);
            TransactionResponse response = new TransactionResponse(
                details.getTransactionId(),
                workflowId,
                "ERROR",
                "Failed to start workflow: " + e.getMessage(),
                details
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{workflowId}/status")
    public ResponseEntity<TransactionResponse> getTransactionStatus(@PathVariable String workflowId) {
        try {
            TransactionWorkflow workflow = workflowClient.newWorkflowStub(TransactionWorkflow.class, workflowId);
            String status = workflow.processTransaction(null);
            
            TransactionResponse response = new TransactionResponse();
            response.setWorkflowId(workflowId);
            response.setStatus(status);
            response.setMessage("Workflow status retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get status for workflow {}: {}", workflowId, e.getMessage(), e);
            TransactionResponse response = new TransactionResponse();
            response.setWorkflowId(workflowId);
            response.setStatus("ERROR");
            response.setMessage("Failed to get workflow status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 