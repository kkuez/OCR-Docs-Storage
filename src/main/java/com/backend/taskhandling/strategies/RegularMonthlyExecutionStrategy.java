package com.backend.taskhandling.strategies;

import com.backend.taskhandling.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class RegularMonthlyExecutionStrategy extends RegularExecutionStrategy {

    public RegularMonthlyExecutionStrategy(Task task, int day) {
        this.task = task;
        min = 0;
        hour = 4;
        this.day = day;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.MONTHLY;
    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return localDateTime.equals(getTime());

    }

    @Override
    public String getInsertDBString() {
        int year = 99;

        int month = 99;

        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0) + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, taskType, strategyType, eID) " +
                "Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + min + ", '" + task.getName()
                + "', '" + user + "', '" + task.getClass().getSimpleName() + "', '" + getType() + "', '"
                + task.geteID() + "')";
    }

    @Override
    public LocalDateTime getTime() {
        return LocalDateTime.of(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), day),
                LocalTime.of(hour, min));
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }
}
