package com.backend.taskhandling.strategies;

import com.backend.taskhandling.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class RegularYearlyExecutionStrategy extends RegularExecutionStrategy {

    public RegularYearlyExecutionStrategy(Task task, LocalDateTime time) {
        super(time);
        this.task = task;
    }


    @Override
    public StrategyType getType() {
        return StrategyType.YEARLY;
    }


    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return localDateTime.equals(getTime()) || localDateTime.isAfter(getTime());
    }


    @JsonIgnore
    @Override
    public String getInsertDBString() {
        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0).getName() + "";

        return "insert into CalendarTasks (name, user, taskType, strategyType, eID, time) " +
                "Values ('" + task.getName()
                + "', '" + user + "', '" + task.getClass().getSimpleName() + "', '" + getType() + "', '"
                + task.geteID() + "', '" + getTime().toString() + "')";

    }

    @Override
    public LocalDateTime getTime() {
        return super.getTime();
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }
}
