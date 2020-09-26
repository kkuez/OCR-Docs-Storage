package com.backend.taskhandling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.backend.BackendFacade;
import com.backend.ObjectHub;
import com.backend.taskhandling.strategies.*;
import com.bot.telegram.Bot;
import com.objectTemplates.User;

public class TaskFactory {

    private static Map<Integer, User> allowedUsersMap = ObjectHub.getInstance().getAllowedUsersMap();

    private static Bot bot = ObjectHub.getInstance().getBot();

    private TaskFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static Task getTask(ResultSet rs, BackendFacade facade) throws SQLException {
        List<User> userList = new ArrayList<>();
        if (rs.getString("user").equals("ALL")) {
            userList.addAll(allowedUsersMap.values());
        } else {
            userList.add(allowedUsersMap.get(Integer.parseInt(rs.getString("user"))));
        }

        Task task;
        String taskType = rs.getString("taskType");
        if ("Task".equals(taskType)) {
            task = new Task(userList, bot, rs.getString("name"));
        } else {
            throw new IllegalStateException("Unexpected value: " + taskType);
        }

        ExecutionStrategy executionStrategy = null;

        String strategyTypeString = rs.getString("strategyType");
        StrategyType strategyType = getStrategyTypeOrNull(strategyTypeString);
        if (strategyType == null) {
            throw new RuntimeException("Couldnt parse StrategyType from DB: " + strategyTypeString);
        }

        switch (strategyType) {
            case SIMPLECALENDAR_ONETIME:
                LocalDateTime time = LocalDateTime.of(rs.getInt("year"), rs.getInt("month"), rs.getInt("day"),
                        rs.getInt("hour"), rs.getInt("minute"));
                executionStrategy = new SimpleCalendarOneTimeStrategy(task, time, facade);
                task.setExecutionStrategy(executionStrategy);
                break;
            case MINUTELY:
                executionStrategy = new RegularMinutelyExecutionStrategy(task);
                break;
            case DAILY:
                executionStrategy = new RegularDailyExecutionStrategy(task);
                break;
            case MONTHLY:
                executionStrategy = new RegularMonthlyExecutionStrategy(task, rs.getInt("day"));
                break;
            case YEARLY:
                executionStrategy = new RegularYearlyExecutionStrategy(task, rs.getInt("day"), rs.getInt("month"));
                break;
            default:
                throw new RuntimeException("Couldnt parse StrategyType from DB: " + strategyTypeString);
        }
        task.setExecutionStrategy(executionStrategy);
        return task;
    }

    public static StrategyType getStrategyTypeOrNull(String taskType) {
        switch (taskType) {
            case "SIMPLECALENDAR_ONETIME":
                return StrategyType.SIMPLECALENDAR_ONETIME;
            case "MINUTELY":
                return StrategyType.MINUTELY;
            case "DAILY":
                return StrategyType.DAILY;
            case "MONTHLY":
                return StrategyType.MONTHLY;
            case "YEARLY":
                return StrategyType.YEARLY;
            case "WEEKLY":
                return StrategyType.WEEKLY;
            default:
                return null;
        }
    }
}
