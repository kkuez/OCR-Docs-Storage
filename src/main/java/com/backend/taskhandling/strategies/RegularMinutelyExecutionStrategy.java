package com.backend.taskhandling.strategies;

import com.backend.taskhandling.Task;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RegularMinutelyExecutionStrategy extends RegularExecutionStrategy {

    public RegularMinutelyExecutionStrategy(Task task) {
        this.task = task;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.MINUTELY;
    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return true;
    }

    @Override
    public String getInsertDBString() {
        int year = 99;

        int month = 99;

        int day = 99;

        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0).getId() + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, taskType, strategyType, eID) " +
                "Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + min + ", '" + task.getName()
                + "', '" + user + "', '" + task.getClass().getSimpleName() + "', '" + getType() + "', '"
                + task.geteID() + "')";
    }

    @Override
    public LocalDateTime getTime() {
        return LocalDateTime.now();
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }
}
