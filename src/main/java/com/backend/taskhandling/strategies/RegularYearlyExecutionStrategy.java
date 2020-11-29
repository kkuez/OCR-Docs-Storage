package com.backend.taskhandling.strategies;

import com.backend.taskhandling.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class RegularYearlyExecutionStrategy extends RegularExecutionStrategy {

    public RegularYearlyExecutionStrategy(Task task, int day, int month) {
        this.task = task;
        min = 0;
        hour = 4;
        this.day = day;
        this.month = month;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.YEARLY;
    }


    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return localDateTime.equals(getTime());
    }


    @JsonIgnore
    @Override
    public String getInsertDBString() {
        int year = 99;

        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0).getId() + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, taskType, strategyType, eID) " +
                "Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + min + ", '" + task.getName()
                + "', '" + user + "', '" + task.getClass().getSimpleName() + "', '" + getType() + "', '"
                + task.geteID() + "')";

    }

    @Override
    public LocalDateTime getTime() {
        return LocalDateTime.of(LocalDate.of(LocalDate.now().getYear(), month, day), LocalTime.of(hour, min));
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }
}
