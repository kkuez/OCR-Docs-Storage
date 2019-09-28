package com.Misc.TaskHandling.Strategies;

public interface TaskStrategy {

     boolean performNow(int currentMinute, int currentHour, String currentDate);

}
