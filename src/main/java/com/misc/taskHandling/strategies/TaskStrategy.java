package com.misc.taskHandling.strategies;

import com.misc.taskHandling.Task;
import com.utils.DBUtil;

import java.time.LocalDateTime;

public interface TaskStrategy {
     String getType();

     boolean timeIsNow(LocalDateTime localDateTime);

     String getInsertDBString();

     LocalDateTime getTime();

    default void delete(String taskName){
            int year = getTime().getYear();
            int month = getTime().getMonth().getValue();
            int day = getTime().getDayOfMonth();
            int hour = getTime().getHour();
            int minute = getTime().getMinute();

            DBUtil.executeSQL("delete from CalendarTasks where name='" + taskName + "' AND year=" + year + " AND month=" + month + " AND day=" + day + " AND hour=" + hour + " AND minute=" + minute);
    }
}
