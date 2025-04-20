package com.example.workflow;

import com.example.workflow.activities.*;
import com.example.workflow.workflow.TransactionWorkflow;
import com.example.workflow.workflow.TransactionWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransactionWorker {
    public static final String TASK_QUEUE = "DEBIT_CARD_TASK_QUEUE";

    public static void main(String[] args) {
        SpringApplication.run(TransactionWorker.class, args);

        // Create a Temporal service client
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);

        // Create a worker factory
        WorkerFactory factory = WorkerFactory.newInstance(client);

        // Create a worker
        Worker worker = factory.newWorker(TASK_QUEUE);

        // Register workflow implementation
        worker.registerWorkflowImplementationTypes(TransactionWorkflowImpl.class);

        // Register all activity implementations
        worker.registerActivitiesImplementations(
            new AccountActivityImpl(),
            new DebitCardActivityImpl(),
            new FraudCheckActivityImpl(),
            new NotificationActivityImpl()
        );

        // Start the worker
        factory.start();

        System.out.println("Worker started for task queue: " + TASK_QUEUE);
    }
} 