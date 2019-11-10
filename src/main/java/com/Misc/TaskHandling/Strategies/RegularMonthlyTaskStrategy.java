package com.Misc.TaskHandling.Strategies;

import com.Misc.TaskHandling.Task;

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
    public String getType() {
        return "RegularMonthlyTaskStrategy";
    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return localDateTime.equals(LocalDateTime.of(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), day), LocalTime.of(hour, min)));

    }

    @Override
    public String getInsertDBString() {
        int year = 99;

        int month = 99;

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
