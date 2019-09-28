package com.Misc.TaskHandling.Strategies;

public class NextPerformanceStrategy implements TaskStrategy {
    @Override
    public boolean performNow(int currentMinute, int currentHour, String currentDate) {
        return true;
    }
}
