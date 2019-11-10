package com.Misc.TaskHandling.Strategies;

import java.time.LocalDateTime;

public interface TaskStrategy {
     String getType();

     boolean timeIsNow(LocalDateTime localDateTime);

     String getInsertDBString();

     LocalDateTime getTime();
}
