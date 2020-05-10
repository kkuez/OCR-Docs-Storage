package com.misc.taskHandling.strategies;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class RegularWeeklyTaskStrategy extends RegularTaskStrategy {
    @Override
    public StrategyType getType() {
        return StrategyType.WEEKLY;
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
        //TODO
        logger.info("Get time of weekly not supported yet :(");
        return LocalDateTime.now();
    }

    @Override
    public TimeUnit getExecutionTimeUnit() {
        return null;
    }
}
