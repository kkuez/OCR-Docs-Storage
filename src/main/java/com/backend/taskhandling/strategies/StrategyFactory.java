package com.backend.taskhandling.strategies;

import com.backend.BackendFacade;
import com.backend.taskhandling.Task;

import java.time.LocalDateTime;

public class StrategyFactory {
    //TODO
    public static ExecutionStrategy getStrategy(StrategyType strategyType, LocalDateTime time, Task task,
                                                BackendFacade facade) {
        switch (strategyType) {
            case MINUTELY:
                return new RegularMinutelyExecutionStrategy(task);
            case DAILY:
                return new RegularDailyExecutionStrategy(task);
            case WEEKLY:
                return new RegularWeeklyExecutionStrategy();
            case YEARLY:
                return new RegularYearlyExecutionStrategy(task, time.getDayOfMonth(), time.getMonthValue());
            case MONTHLY:
                return new RegularMonthlyExecutionStrategy(task, time.getDayOfMonth());
            case SIMPLECALENDAR_ONETIME:
            case ONETIME:
                return new SimpleCalendarOneTimeStrategy(task, time, facade);
            default:
                throw new RuntimeException("No ExecutionStrategy could be assigned!");
        }
    }
}
