# camunda-external-task-worker

This project demonstrates using the Camunda 7 External Task Worker Client with Spring Boot.  
It allows you to test three different scenarios:

1. **Successful Task Execution:** Simulates a successful completion of the task
2. **Technical Task Error:** Triggers a technical error
3. **Business Error (BPMN Error):** Triggers a BPMN error

In case of a technical error an exponential backoff strategy is applied to the task execution. If the maximum retry count is reached, the task is marked as failed and an incident is created.

**BPMN Process:**

The `bpmn` folder contains a sample BPMN process definition for this example.

**Running the Project:**

1. Configure the `base_url` setting with your Camunda Rest-API URL.
2. Deploy the BPMN process to your Camunda 7 instance.
3. Build and run the project using Maven/Cockpit GUI.

