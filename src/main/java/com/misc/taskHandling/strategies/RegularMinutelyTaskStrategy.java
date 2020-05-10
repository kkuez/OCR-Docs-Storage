package com.misc.taskHandling.strategies;

import com.misc.taskHandling.Task;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class RegularMinutelyTaskStrategy extends RegularTaskStrategy {

    public RegularMinutelyTaskStrategy(Task task) {
        this.task = task;
    }

    @Override
    public String getType() {
        return "RegularMinutelyTaskStrategy";
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

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, taskType, strategyType) Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + min + ", '" + task.getName() + "', '" + user + "', '" + task.getClass().getSimpleName() + "', '" + getType() + "')";
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
