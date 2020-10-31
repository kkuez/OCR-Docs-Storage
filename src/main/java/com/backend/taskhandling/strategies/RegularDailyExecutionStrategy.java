package com.backend.taskhandling.strategies;

import com.backend.taskhandling.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class RegularDailyExecutionStrategy extends RegularExecutionStrategy {

    public RegularDailyExecutionStrategy(Task task) {
        this.task = task;
        min = 0;
        hour = 4;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.DAILY;
    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return localDateTime.equals(getTime());
    }

    @Override
    public String getInsertDBString() {
        int year = 99;

        int month = 99;

        int day = 99;

        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0).getId() + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, taskType, strategyType) Values ("
                + year + ", " + month + ", " + day + ", " + hour + ", " + min + ", '" + task.getName() + "', '" + user
                + "', '" + task.getClass().getSimpleName() + "', '" + getType() + "')";

    }

    @Override
    public LocalDateTime getTime() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, min));
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }
}
