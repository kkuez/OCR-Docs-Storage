package com.Misc.TaskHandling.Strategies;

import java.time.LocalDateTime;

public interface TaskStrategy {
     String getType();

     boolean perform();

     String getStrategyName();
}
