package com.Misc.TaskHandling.Strategies;

public class NextPerformanceStrategy implements TaskStrategy {
    @Override
    public boolean perform(int currentMinute, int currentHour, String currentDate) {
        return true;
    }
}
