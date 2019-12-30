package com.misc.taskHandling.strategies;

import com.Main;
import com.misc.taskHandling.Task;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public abstract class RegularTaskStrategy implements TaskStrategy {
    int min = 0;

    int hour = 0;

    int day;

    int month;

    Task task;

    private static Logger logger = Main.logger;

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
