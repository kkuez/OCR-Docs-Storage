package com.Misc.TaskHandling.Strategies;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class OneTimeTaskStrategy implements TaskStrategy {

    LocalDateTime timeToPerform;

    public OneTimeTaskStrategy(){}

    public OneTimeTaskStrategy(int year, int month, int day){
        this.timeToPerform = LocalDateTime.of(year, month, day, 0,0);
    }

    public OneTimeTaskStrategy(int hourToPerform, int minuteToPerform, int year, int month, int day){
        this.timeToPerform = LocalDateTime.of(year, month, day, hourToPerform, minuteToPerform);
    }

    @Override
    public void perform() {

    }

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return timeToPerform.equals(localDateTime);
    }
}
