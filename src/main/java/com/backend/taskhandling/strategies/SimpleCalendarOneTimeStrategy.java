package com.backend.taskhandling.strategies;

import com.backend.BackendFacade;
import com.backend.taskhandling.Task;

import java.time.LocalDateTime;

public class SimpleCalendarOneTimeStrategy extends OneTimeExecutionStrategy {

    private LocalDateTime time;

    public SimpleCalendarOneTimeStrategy(Task task, LocalDateTime time, BackendFacade facade) {
        super(facade, task);
        this.time = time;
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

        String user = getTask().getUserList().size() > 1 ? "ALL" : getTask().getUserList().get(0) + "";

        return "insert into CalendarTasks (year, month, day, hour, minute, name, user, taskType, strategyType, eID) " +
                "Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", '"
                + getTask().getName() + "', '" + user + "', '" + getTask().getClass().getSimpleName() + "', '"
                + getType() + "', '" + getTask().geteID() + "')";
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
