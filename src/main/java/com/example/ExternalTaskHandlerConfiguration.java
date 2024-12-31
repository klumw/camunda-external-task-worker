package com.example;

import org.camunda.bpm.client.spring.SpringTopicSubscription;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.spring.boot.starter.ClientProperties;
import org.camunda.bpm.client.spring.event.SubscriptionInitializedEvent;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class ExternalTaskHandlerConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(ExternalTaskHandlerConfiguration.class);

  protected String workerId;

  protected BackOffStrategy backOff;

  public enum ErrorTypes {
    TECHNICAL_ERROR,
    BUSINESS_ERROR,
    NO_ERROR
  };

  protected ErrorTypes errorType = ErrorTypes.TECHNICAL_ERROR; //Set test scenario here

  /**
   * Backoff strategy bean
   * @param backOff
   */
  @Autowired
  public void setBackOffStrategy(BackOffStrategy backOff) {
    this.backOff = backOff;
  }

  public ExternalTaskHandlerConfiguration(ClientProperties properties) {
    workerId = properties.getWorkerId();
  }

  /**
   * @return ExternalTaskHandler
   */
  @ExternalTaskSubscription(topicName = "printVariables")
  @Bean
  public ExternalTaskHandler printVariables() {
    return (externalTask, externalTaskService) -> {
      System.out.println("----------Process Variables------------------");
      externalTask.getAllVariables().forEach((key, value) -> {
        System.out.println(key + " : " + value);
      });
      if (!externalTask.getExtensionProperties().isEmpty()) {
        System.out.println("--------Extension Properties---------------");
        externalTask.getExtensionProperties().forEach((key, value) -> {
          System.out.println(key + " : " + value);
        });
      }
      handleSuccess(externalTask, externalTaskService);
    };
  }

  /**
   * @return ExternalTaskHandler
   */
  @ExternalTaskSubscription("testActivity")
  @Bean
  public ExternalTaskHandler failure() {
    return (externalTask, externalTaskService) -> {
      try {
        switch (errorType) {
          case TECHNICAL_ERROR:
            throw new RuntimeException("A technical error occurred!");
          case BUSINESS_ERROR:
            handleBusinessFailure(externalTask, externalTaskService, "ERR001",
                "This is a business error message");
            break;
          default:
            handleSuccess(externalTask, externalTaskService);
            break;
        }
      } catch (Exception e) {
        handleTechnicalFailure(externalTask, externalTaskService, e, "ERR800");
      }
    };
  }

  protected void handleSuccess(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    externalTaskService.complete(externalTask);
    LOG.info("{}: External Task id: {} with process id: {} completed successfully", workerId, externalTask.getId(),
        externalTask.getProcessInstanceId());
  }

  protected void handleTechnicalFailure(ExternalTask externalTask, ExternalTaskService externalTaskService, Exception e,
        String errorCode) {
      int retries = backOff.getNextRetryCount(externalTask);
      externalTaskService.handleFailure(externalTask, e.getLocalizedMessage(), "put ErrorDetails here", retries, backOff.getNextRetryDelay(externalTask));
      LOG.error("{}: External Task id: {} with process id: {} failed with error code:{}! Active retries: {}, timeout: {}", workerId, externalTask.getId(), externalTask.getProcessInstanceId(), errorCode, retries, backOff.getNextRetryDelay(externalTask), e);
  }

  protected void handleBusinessFailure(ExternalTask externalTask, ExternalTaskService externalTaskService, String errorCode, String errorMessage) {
      externalTaskService.handleBpmnError(externalTask, errorCode, errorMessage);
      LOG.error("{}: External Task id: {} failed with business error code:{}!", workerId, externalTask.getId(), errorCode);
  }

  /**
   * @param event
   */
  @EventListener(SubscriptionInitializedEvent.class)
  public void catchSubscriptionInitEvent(SubscriptionInitializedEvent event) {

    SpringTopicSubscription topicSubscription = event.getSource();
    if (!topicSubscription.isAutoOpen()) {
      topicSubscription.open();
      LOG.info("Subscription with topic name '{}' has been opened!",
          topicSubscription.getTopicName());
    }
  }

}
