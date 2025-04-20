package com.example.workflow.config;

import com.example.workflow.activities.*;
import com.example.workflow.workflow.TransactionWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {
    private static final Logger log = LoggerFactory.getLogger(TemporalConfig.class);
    public static final String TASK_QUEUE = "DEBIT_CARD_TASK_QUEUE";

    @Bean
    public WorkflowClient workflowClient() {
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        return WorkflowClient.newInstance(service);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);

        // Register workflow implementation
        worker.registerWorkflowImplementationTypes(TransactionWorkflowImpl.class);

        // Register activity implementations
        worker.registerActivitiesImplementations(
            new AccountActivityImpl(),
            new DebitCardActivityImpl(),
            new FraudCheckActivityImpl(),
            new NotificationActivityImpl()
        );

        // Start the worker factory
        factory.start();
        log.info("Worker factory started for task queue: {}", TASK_QUEUE);

        return factory;
    }
} 