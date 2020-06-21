package com.backend.taskHandling.strategies;

import com.Main;
import com.backend.taskHandling.Task;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public abstract class RegularExecutionStrategy implements ExecutionStrategy {

    Logger LOGGER = Main.getLogger();

    int min = 0;

    int hour = 0;

    int day;

    int month;

    Task task;

    public Logger logger = Main.getLogger();

    public RegularExecutionStrategy(){
    }

    public abstract TimeUnit getExecutionTimeUnit();

    public void delete(String taskName) {
        //TODO
        Logger.getLogger("Deletion of regular tasks not supported... yet :)");
    };

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
