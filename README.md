# camunda-external-task-worker

This project demonstrates using the Camunda 7 External Task Worker Client with Spring Boot.  
It allows you to test three different scenarios:

1. **Successful Task Execution:** Simulates a successful completion of the task.
2. **Technical Task Error:** Triggers a technical error during task execution.
3. **Business Error (BPMN Error):** Throws a BPMN error within the task.

**Prerequisites:**

* A running Camunda 7 instance.
* Adapt the `base_url` setting to match your Camunda 7 instance.

**BPMN Process:**

The `bpmn` folder contains a sample BPMN process definition for this example.

**Running the Project:**

1. Configure the `base_url` setting with your Camunda Rest-API URL.
2. Deploy the BPMN process to your Camunda 7 instance.
3. Build and run the project using Maven/Cockpit GUI.

