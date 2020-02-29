package com.misc.taskHandling.strategies;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class RegularWeeklyTaskStrategy extends RegularTaskStrategy {
    @Override
    public String getType() {
        return null;
    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return false;
    }

    @Override
    public String getInsertDBString() {
        return null;
        //TODO implementieren die ganze Klasse ffs
    }

    @Override
    public LocalDateTime getTime() {
        return null;
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }
}
