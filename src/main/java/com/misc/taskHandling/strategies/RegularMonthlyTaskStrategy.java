package com.misc.taskHandling.strategies;

import com.misc.taskHandling.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class RegularMonthlyTaskStrategy extends RegularTaskStrategy {

    public RegularMonthlyTaskStrategy(Task task, int day){
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

        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0).getId() + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, taskType, strategyType) Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + min + ", '" + task.getName() + "', '" + user + "', '" + task.getClass().getSimpleName() + "', '" + getType() +"')";
    }

    @Override
    public LocalDateTime getTime() {
        return LocalDateTime.of(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), day), LocalTime.of(hour, min));
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }
}
