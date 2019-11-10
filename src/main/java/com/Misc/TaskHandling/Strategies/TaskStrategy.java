package com.Misc.TaskHandling.Strategies;

import java.time.LocalDateTime;

public interface TaskStrategy {
     String getType();

     boolean perform();

     boolean timeIsNow(LocalDateTime localDateTime);

     String getInsertDBString();

     String getStrategyName();

     LocalDateTime getTime();
}
