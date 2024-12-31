package com.example;

import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/*
  The BackOff class is a Spring component designed to manage exponential backoff 
  strategies for retrying external tasks in a Camunda BPM application. 
*/
@Component
public class BackOffStrategy {

  protected long initTime;
  protected float factor;
  protected long maxTime;
  protected int maxRetries;

  public BackOffStrategy(@Value("${backoff.initTime}") long initTime, @Value("${backoff.factor}") float factor,
      @Value("${backoff.maxTime}") long maxTime, @Value("${backoff.maxRetries}") int retries) {
    this.initTime = initTime * 2; //Needs to be doubled to get the correct delay
    this.factor = factor;
    this.maxTime = maxTime;
    this.maxRetries = retries;
  }

  /**
   * Return the new delay time in milliseconds.
   * @param externalTask
   * @return long
   */
  public long getNextRetryDelay(ExternalTask externalTask) {
    int retryCount = (externalTask.getRetries() == null) ? maxRetries : externalTask.getRetries();
    long level = maxRetries - retryCount;
    long backoffTime = (long) (initTime * Math.pow(factor, level - 1));
    return Math.min(backoffTime, maxTime);
  }

  
  /** 
   * Return the next retry count.
   * @param externalTask
   * @return int
   */
  public int getNextRetryCount(ExternalTask externalTask) {
    return (externalTask.getRetries() == null) ? maxRetries : externalTask.getRetries() - 1;
  }
}
