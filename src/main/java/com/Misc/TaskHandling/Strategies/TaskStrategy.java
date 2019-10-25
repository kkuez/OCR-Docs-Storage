package com.Misc.TaskHandling.Strategies;

import java.time.LocalDateTime;

public interface TaskStrategy {

     void perform();

     String getStrategyName();
}
