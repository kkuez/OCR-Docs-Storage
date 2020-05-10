package com.misc.taskHandling;

import com.ObjectHub;
import com.misc.taskHandling.strategies.*;
import com.objectTemplates.User;
import com.telegram.Bot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskFactory {

    private static Map<Integer, User> allowedUsersMap = ObjectHub.getInstance().getAllowedUsersMap();
    private static Bot bot = ObjectHub.getInstance().getBot();

    public static Task getTask(ResultSet rs) throws SQLException {
        String strategyType = rs.getString("strategyType");
        List<User> userList = new ArrayList<>();
        if(rs.getString("user").equals("ALL")){
            userList.addAll(allowedUsersMap.values());
        }else{
            userList.add(allowedUsersMap.get(Integer.parseInt(rs.getString("user"))));
        }

        Task task;
        String taskType = rs.getString("taskType");
        switch (taskType){
            case "Task":
                task = new Task(userList, bot, rs.getString("name"));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + taskType);
        }

        TaskStrategy taskStrategy = null;
        switch (strategyType){
            case "SimpleCalendarOneTimeStrategy":
                LocalDateTime time = LocalDateTime.of(rs.getInt("year"),rs.getInt("month"),rs.getInt("day"),rs.getInt("hour"),rs.getInt("minute"));
                taskStrategy = new SimpleCalendarOneTimeStrategy(task, time);
                task.setTaskStrategy(taskStrategy);
                break;
            case "RegularMinutelyTaskStrategy":
                taskStrategy = new RegularMinutelyTaskStrategy(task);
                break;
            case "RegularDailyTaskStrategy":
                taskStrategy = new RegularDailyTaskStrategy(task);
                break;
            case "RegularMonthlyTaskStrategy":
                taskStrategy = new RegularMonthlyTaskStrategy(task, rs.getInt("day"));
                break;
            case "RegularYearlyTaskStrategy":
                taskStrategy = new RegularYearlyTaskStrategy(task, rs.getInt("day"), rs.getInt("month"));
                break;
        }
        task.setTaskStrategy(taskStrategy);
        return task;
    }

}
