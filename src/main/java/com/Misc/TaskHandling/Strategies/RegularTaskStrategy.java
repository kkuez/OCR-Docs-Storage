package com.Misc.TaskHandling.Strategies;

import java.time.LocalDate;

public class RegularTaskStrategy extends OneTimeTaskStrategy {

    int daysFrequency = 0;

    int hoursFrequency = 0;

    int minutesFrequency = 0;

    public RegularTaskStrategy(int minutesFrequency, int hoursFrequency, int daysFrequency){
        this.minutesFrequency = minutesFrequency;
        this.hoursFrequency = hoursFrequency;
        this.daysFrequency = daysFrequency;
    }

    private void setNextPerformTime(){
        if(minuteToPerform != 99)
        {
            minuteToPerform += minutesFrequency;
            hourToPerform += hoursFrequency;
            dateToPerform = LocalDate.now().plusDays(daysFrequency).toString();
        }
    }

    private void doAfterExecute(){
        setNextPerformTime();
    }
}