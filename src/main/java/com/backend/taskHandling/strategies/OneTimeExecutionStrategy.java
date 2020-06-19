package com.backend.taskHandling.strategies;

import com.Main;
import com.backend.taskHandling.Task;
import com.utils.DBUtil;
import org.apache.log4j.Logger;

public abstract class OneTimeExecutionStrategy implements ExecutionStrategy {
    private static Logger logger = Main.getLogger();

    private String name;

    Task task;

    public OneTimeExecutionStrategy(){}

    @Override
    public StrategyType getType() {
        return StrategyType.ONETIME;
    }

    public void delete(String taskName){
        int year = getTime().getYear();
        int month = getTime().getMonth().getValue();
        int day = getTime().getDayOfMonth();
        int hour = getTime().getHour();
        int minute = getTime().getMinute();

        DBUtil.executeSQL("delete from CalendarTasks where name='" + taskName + "' AND year=" + year + " AND month=" + month + " AND day=" + day + " AND hour=" + hour + " AND minute=" + minute);
    }

    //GETTER SETTER

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
