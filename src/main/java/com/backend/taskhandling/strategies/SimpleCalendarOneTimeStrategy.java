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

        String user = getTask().getUserList().size() > 1 ? "ALL" : getTask().getUserList().get(0).getName();

        return "insert into CalendarTasks (name, user, taskType, strategyType, eID; time) " +
                "Values ('" + getTask().getName() + "', '" + user + "', '" + getTask().getClass().getSimpleName() + "', '"
                + getType() + "', '" + getTask().geteID() + "', ' " + getTime().toString() + "')";
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
