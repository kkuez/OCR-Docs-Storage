package com.misc.taskHandling.strategies;

import com.misc.taskHandling.Task;

import java.time.LocalDateTime;

public class SimpleCalendarOneTimeStrategy extends OneTimeTaskStrategy {

    private Task task;

    private LocalDateTime time;

    public SimpleCalendarOneTimeStrategy(Task task, LocalDateTime time)
    {   this.time = time;
        this.task = task;
    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
            return time.equals(localDateTime) || time.isBefore(localDateTime);
    }

    @Override
    public String getInsertDBString() {
        int year = time.getYear();

        int month = time.getMonth().getValue();

        int day = time.getDayOfMonth();

        int hour = time.getHour();

        int minute = time.getMinute();

        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0).getId() + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, taskType, strategyType) Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", '" + task.getName() + "', '" + user + "', '" + task.getClass().getSimpleName() + "', '" + getType() +"')";
    }

    @Override
    public StrategyType getType() {
        return StrategyType.SIMPLECALENDAR_ONETIME;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
