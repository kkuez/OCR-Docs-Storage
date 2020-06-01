package com.misc.taskHandling.strategies;

import com.misc.taskHandling.Task;
import com.utils.DBUtil;

import java.time.LocalDateTime;

public interface ExecutionStrategy {
     StrategyType getType();

     boolean timeIsNow(LocalDateTime localDateTime);

     String getInsertDBString();

    void delete(String taskName);

    LocalDateTime getTime();
}
