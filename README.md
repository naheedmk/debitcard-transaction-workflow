# Debit Card Transaction Workflow - Temporal Java Example

This project demonstrates a prepaid debit card transaction workflow using the Temporal Java SDK and Spring Boot.

## Features

*   **HTTP Endpoint:** Exposes a REST API (`POST /api/transactions`) to trigger new transaction workflows.
*   **Parallel Validation:** Checks checking account balance and debit card limit/funds concurrently.
*   **Parallel Memo Posting:** Places holds on both the checking account and debit card system concurrently.
*   **Asynchronous Fraud Check:** Initiates a simulated, potentially long-running fraud check in parallel with memo posting.
*   **Randomized Fraud Simulation:** The fraud check activity introduces a random delay and randomly flags transactions as fraudulent (approx. 10% chance).
*   **Saga Pattern for Compensation:** If fraud is detected after memo posts have occurred, the workflow automatically compensates by reversing the memo posts.
*   **Final Posting:** If the transaction is valid and not fraudulent, final debits are posted.
*   **Notifications:** Sends a simple notification upon success or fraud detection/reversal.

## Prerequisites

*   Java Development Kit (JDK) 11 or later
*   Apache Maven
*   Temporal Server running (v1.17+ recommended). You can use [Temporal CLI](https://docs.temporal.io/cli) (`temporal server start-dev`) for local development.
*   `curl` or a tool like Postman for sending HTTP requests.

## Building the Project

Use Maven to compile the project and create an executable JAR:

```bash
mvn clean package
```

This will generate a JAR file in the `target/` directory (e.g., `debitcard-transaction-workflow-1.0-SNAPSHOT.jar`).

## Running the Application

1.  **Start the Temporal Server:**
    Make sure your Temporal development server is running. If using Temporal CLI:
    ```bash
    temporal server start-dev
    ```

2.  **Start the Worker:**
    Open a terminal and run the `TransactionWorker` main class from the generated JAR. The worker processes the background tasks initiated by the workflow.
    ```bash
    # Make sure you are in the project's root directory
    java -cp target/debitcard-transaction-workflow-1.0-SNAPSHOT.jar com.example.workflow.TransactionWorker
    ```
    The worker will connect to Temporal, register the workflow and activities, and start listening for tasks on the `debit-card-transaction-queue`.

3.  **Start the Spring Boot Application (API):**
    Open *another* terminal and run the Spring Boot application. This application provides the HTTP endpoint.
    ```bash
    # Make sure you are in the project's root directory
    java -jar target/debitcard-transaction-workflow-1.0-SNAPSHOT.jar
    ```
    Alternatively, you can run it directly via Maven:
    ```bash
    mvn spring-boot:run
    ```
    The application will start, usually on port 8080.

4.  **Trigger a Workflow via API:**
    Open a *third* terminal (or use Postman) to send a POST request to the `/api/transactions` endpoint. 

    **Using `curl`:**
    ```bash
    curl -X POST http://localhost:8080/api/transactions \
    -H "Content-Type: application/json" \
    -d '{
          "transactionId": "my-txn-001", 
          "accountId": "account-123", 
          "cardId": "card-abc", 
          "amount": 55.75, 
          "customerId": "customer-api"
        }'
    ```
    You should receive a JSON response containing the `workflowId`:
    ```json
    {"workflowId":"debit-card-txn-my-txn-001"}
    ```

    **Using Postman:**
    *   Set the request type to `POST`.
    *   Set the URL to `http://localhost:8080/api/transactions`.
    *   Go to the `Body` tab, select `raw`, and choose `JSON` from the dropdown.
    *   Paste the JSON payload into the body:
        ```json
        {
            "transactionId": "my-txn-002",
            "accountId": "account-456",
            "cardId": "card-abc",
            "amount": 60.00,
            "customerId": "customer-postman"
        }
        ```
    *   Send the request. You'll get the `workflowId` in the response.

5.  **Observe:**
    *   Check the logs in the **Worker** terminal to see the workflow and activity execution details.
    *   Check the logs in the **Spring Boot Application** terminal to see the incoming API requests and workflow start logs.
    *   Use the Temporal Web UI (usually `http://localhost:8088`) to view the workflow executions (`debit-card-txn-my-txn-001`, etc.), their history, and status.

## Project Structure

*   `pom.xml`: Maven project configuration and dependencies.
*   `src/main/java/com/example/workflow/`: Root package.
    *   `DebitCardWorkflowApplication.java`: Spring Boot main application class, Temporal client config.
    *   `Shared.java`: Shared constants (like task queue name).
    *   `TransactionWorker.java`: Main class to run the Temporal worker.
    *   `controller/`: Spring Web controllers.
        *   `TransactionController.java`: REST API endpoint for starting transactions.
    *   `model/`: Data transfer objects.
        *   `TransactionDetails.java`: POJO for workflow input / API request body.
    *   `activities/`: Activity interfaces and implementations.
        *   `AccountActivity.java` / `AccountActivityImpl.java`
        *   `CheckingAccountLedger.java`
        *   `DebitCardActivity.java` / `DebitCardActivityImpl.java`
        *   `DebitCardLedger.java`
        *   `FraudCheckActivity.java` / `FraudCheckActivityImpl.java`
        *   `NotificationActivity.java` / `NotificationActivityImpl.java`
    *   `workflow/`: Workflow interface and implementation.
        *   `TransactionWorkflow.java` / `TransactionWorkflowImpl.java` 


        Let me summarize the test results:
testSuccessfulTransaction (test-txn-001):
Successfully validated account balance and card limit
Completed memo posts
Passed fraud check
Completed final posts
Sent success notification
testInsufficientFunds (test-txn-002):
Failed validation due to insufficient account balance
No further processing occurred
testFraudulentTransaction (test-txn-003):
Successfully validated account balance and card limit
Completed memo posts
Passed fraud check
Completed final posts
Sent success notification
testMissingRequiredFields (test-txn-004):
Failed immediately due to missing required fields
No further processing occurred
testLargeTransaction (test-txn-005):
Failed validation due to insufficient account balance
No further processing occurred
The test suite completed in 8.655 seconds with: