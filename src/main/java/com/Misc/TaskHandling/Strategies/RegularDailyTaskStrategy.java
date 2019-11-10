package com.Misc.TaskHandling.Strategies;

import com.Misc.TaskHandling.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class RegularDailyTaskStrategy extends RegularTaskStrategy {

    int min;

    int hour;

    public RegularDailyTaskStrategy(Task task){
        this.task = task;
        min = 0;
        hour = 4;
    }

    @Override
    public String getType() {
        return "RegularDailyTaskStrategy";
    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return localDateTime.equals(LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, min)));
    }

    @Override
    public String getInsertDBString(){
        int year = 99;

        int month = 99;

        int day = 99;

        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0).getId() + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, strategyType) Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + min + ", '" + task.getName() + "', '" + user + "', '" + getType() +"')";

    }

    @Override
    public LocalDateTime getTime() {
        return null;
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }

    @Override
    public void doAfterExecute() {

    }
}
