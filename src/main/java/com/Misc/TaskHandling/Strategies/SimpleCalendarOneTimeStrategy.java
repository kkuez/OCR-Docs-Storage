package com.Misc.TaskHandling.Strategies;

import com.Misc.TaskHandling.Task;

import java.time.LocalDateTime;

public class SimpleCalendarOneTimeStrategy extends OneTimeTaskStrategy {

    Task task;

    public SimpleCalendarOneTimeStrategy(Task task){
        this.task = task;
    }

    @Override
    public void perform() {
    }

    @Override
    public String getStrategyName() {
        return "Einmaliger Termin";
    }
}
