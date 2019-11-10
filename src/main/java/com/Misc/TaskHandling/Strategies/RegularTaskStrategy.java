package com.Misc.TaskHandling.Strategies;

import com.Misc.TaskHandling.Task;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

public abstract class RegularTaskStrategy implements TaskStrategy {
    int min;

    int hour;

    int day;

    int month;

    Task task;

    public RegularTaskStrategy(){
    }

    @Override
    public abstract String getType();

    public abstract TimeUnit getExecutionTimeUnit();

    public abstract void doAfterExecute();

    //GETTER SETTER

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
