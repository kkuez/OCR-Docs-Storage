package com.Misc.TaskHandling.Strategies;

import com.Misc.TaskHandling.Task;
import com.ObjectTemplates.User;
import com.Telegram.KeyboardFactory;

import java.time.LocalDateTime;

public class SimpleCalendarOneTimeStrategy extends OneTimeTaskStrategy {

    Task task;

    private LocalDateTime time;

    public SimpleCalendarOneTimeStrategy(Task task, LocalDateTime time)
    {   this.time = time;
        this.task = task;
    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
            return time.equals(localDateTime);
    }

    @Override
    public String getInsertDBString() {
        int year = time.getYear();

        int month = time.getMonth().getValue();

        int day = time.getDayOfMonth();

        int hour = time.getHour();

        int minute = time.getMinute();

        String user = task.getUserList().size() > 1 ? "ALL" : task.getUserList().get(0).getId() + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, strategyType) Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", '" + task.getName() + "', '" + user + "', '" + getType() +"')";
    }


    @Override
    public String getType() {
        return "SimpleCalendarOneTimeStrategy";
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
