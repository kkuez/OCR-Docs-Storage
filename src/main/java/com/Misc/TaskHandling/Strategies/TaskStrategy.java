package com.Misc.TaskHandling.Strategies;

public abstract class TaskStrategy {

    abstract boolean performNow(int currentMinute, int currentHour, String currentDate);

}
