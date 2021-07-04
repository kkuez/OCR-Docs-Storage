package com.backend.taskhandling.strategies;

import com.StartUp;
import com.backend.taskhandling.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public abstract class RegularExecutionStrategy implements ExecutionStrategy {

    int min = 0;

    int hour = 0;

    int day;

    int month;

    @JsonIgnore
    Task task;

    @JsonIgnore
    public Logger logger = StartUp.createLogger(RegularExecutionStrategy.class);

    public RegularExecutionStrategy() {
    }

    public abstract TimeUnit getExecutionTimeUnit();

    public void delete(String taskName) {
        // TODO
        Logger.getLogger("Deletion of regular tasks not supported... yet :)");
    };

    // GETTER SETTER

    public int getMin() {
        return min;
    }

    public int getHour() {
        return hour;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public Task getTask() {
        return task;
    }
}
