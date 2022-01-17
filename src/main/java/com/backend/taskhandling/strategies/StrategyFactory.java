package com.backend.taskhandling.strategies;

import com.backend.BackendFacade;
import com.backend.taskhandling.Task;

import java.time.LocalDateTime;

public class StrategyFactory {
    public static ExecutionStrategy getStrategy(StrategyType strategyType, LocalDateTime time, Task task,
                                                BackendFacade facade) {
        switch (strategyType) {
            case DAILY:
                return new RegularDailyExecutionStrategy(task, time);
            case WEEKLY:
                return new RegularWeeklyExecutionStrategy(task, time);
            case YEARLY:
                return new RegularYearlyExecutionStrategy(task, time);
            case MONTHLY:
                return new RegularMonthlyExecutionStrategy(task, time);
            case SIMPLECALENDAR_ONETIME:
            case ONETIME:
                return new SimpleCalendarOneTimeStrategy(task, time, facade);
            default:
                throw new RuntimeException("No ExecutionStrategy could be assigned!");
        }
    }
}
