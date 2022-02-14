package com.backend.taskhandling;

import com.backend.BackendFacade;
import com.backend.taskhandling.strategies.*;
import com.data.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskFactory {


    private Map<String, User> allowedUsersMap;

    @Lazy
    TaskFactory() {
        allowedUsersMap = null;
    }

    public Task createTask(List<User> users, String taskText) {
        return new Task(users, taskText, UUID.randomUUID());
    }

    public Task getTask(ResultSet rs, BackendFacade facade) throws SQLException {
        List<User> userList = new ArrayList<>();
        if (rs.getString("user").equals("ALL")) {
            userList.addAll(allowedUsersMap.values());
        } else {
            userList.add(allowedUsersMap.get(rs.getString("user")));
        }

        Task task;
        String taskType = rs.getString("taskType");
        if ("Task".equals(taskType)) {
            UUID eID = UUID.fromString(rs.getString("eID"));
            task = new Task(userList, rs.getString("name"), eID);
        } else {
            throw new IllegalStateException("Unexpected value: " + taskType);
        }

        ExecutionStrategy executionStrategy;

        String strategyTypeString = rs.getString("strategyType");
        StrategyType strategyType = getStrategyTypeOrNull(strategyTypeString);
        if (strategyType == null) {
            throw new RuntimeException("Couldnt parse StrategyType from DB: " + strategyTypeString);
        }

        LocalDateTime time = LocalDateTime.parse(rs.getString("time"));
        switch (strategyType) {
            case SIMPLECALENDAR_ONETIME:
                executionStrategy = new SimpleCalendarOneTimeStrategy(task, time, facade);
                task.setExecutionStrategy(executionStrategy);
                break;
            case DAILY:
                executionStrategy = new RegularDailyExecutionStrategy(task, time);
                break;
            case WEEKLY:
                executionStrategy = new RegularWeeklyExecutionStrategy(task, time);
                break;
            case MONTHLY:
                executionStrategy = new RegularMonthlyExecutionStrategy(task, time);
                break;
            case YEARLY:
                executionStrategy = new RegularYearlyExecutionStrategy(task, time);
                break;
            default:
                throw new RuntimeException("Couldnt parse StrategyType from DB: " + strategyTypeString);
        }
        task.setExecutionStrategy(executionStrategy);
        return task;
    }

    public StrategyType getStrategyTypeOrNull(String taskType) {
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

    public void setAllowedUsersMap(Map<String, User> allowedUsersMap) {
        this.allowedUsersMap = allowedUsersMap;
    }

}
