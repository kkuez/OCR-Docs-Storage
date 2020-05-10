package com.misc.taskHandling.strategies;

import com.Main;
import com.misc.taskHandling.Task;
import com.utils.DBUtil;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;

public abstract class OneTimeTaskStrategy implements TaskStrategy {
    private static Logger logger = Main.getLogger();

    private String name;

    Task task;

    public OneTimeTaskStrategy(){}

    @Override
    public String getType() {
        return "OneTimeTaskStrategy";
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
