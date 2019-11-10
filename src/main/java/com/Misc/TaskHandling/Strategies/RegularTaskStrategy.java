package com.Misc.TaskHandling.Strategies;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

public abstract class RegularTaskStrategy implements TaskStrategy {


    public RegularTaskStrategy(){
    }


    @Override
    public abstract String getType();

    @Override
    public abstract boolean perform();

    @Override
    public abstract String getStrategyName();

    public abstract TimeUnit getExecutionTimeUnit();

    public abstract void doAfterExecute();
}
