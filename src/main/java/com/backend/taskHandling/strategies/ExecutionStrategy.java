package com.backend.taskHandling.strategies;

import java.time.LocalDateTime;

public interface ExecutionStrategy {
     StrategyType getType();

     boolean timeIsNow(LocalDateTime localDateTime);

     String getInsertDBString();

    void delete(String taskName);

    LocalDateTime getTime();
}
