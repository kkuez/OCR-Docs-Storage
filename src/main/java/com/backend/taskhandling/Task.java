package com.backend.taskhandling;

import com.backend.taskhandling.strategies.ExecutionStrategy;
import com.backend.taskhandling.strategies.OneTimeExecutionStrategy;
import com.backend.taskhandling.strategies.StrategyType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.data.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task implements Comparable {

    private ExecutionStrategy executionStrategy;

    private String name;

    //@JsonSerialize(using = UUIDToString.class, as=String.class)
    private UUID eID;

    @JsonIgnore
    private List<User> userList = new ArrayList<>();

    public Task(){}


    public Task(List<User> userList,  String actionName, UUID eID) {
        this.userList = userList;
        this.name = actionName;
        this.eID = eID;
    }

    public boolean timeIsNow(LocalDateTime localDateTime) {
        return executionStrategy.timeIsNow(localDateTime);
    }

    @Override
    public int compareTo(Object o) throws RuntimeException {
        if (!o.getClass().equals(this.getClass())) {
            throw new RuntimeException("Classes dont match!");
        }
        if (!(this.getExecutionStrategy() instanceof OneTimeExecutionStrategy)
                || !(((Task) o).getExecutionStrategy() instanceof OneTimeExecutionStrategy)) {
            return 1;
        }

        Task oTask = (Task) o;
        return getExecutionStrategy().getTime().compareTo(oTask.getExecutionStrategy().getTime());
    }

    public void delete() {
        executionStrategy.delete(getName());
    }

    // GETTER SETTER
    public StrategyType getType() {
        return executionStrategy.getType();
    }

    public String getTimeString() {
        return executionStrategy.getTime().toString();
    }

    @JsonIgnore
    public String getInsertDBString() {
        return executionStrategy.getInsertDBString();
    }

    @JsonIgnore
    public ExecutionStrategy getExecutionStrategy() {
        return executionStrategy;
    }

    public void setExecutionStrategy(ExecutionStrategy executionStrategy) {
        this.executionStrategy = executionStrategy;
    }

    public String getName() {
        return name;
    }

    public String getNameString() {
        return name.replace(" ", "%x20");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForWhom() {
        return userList.size() > 1 ? "All" : String.valueOf(userList.get(0).getName());
    }

    public UUID geteID() {
        return eID;
    }

    public boolean perform() {
        //TODO ERINNERUNG SCHREIBEN!
        return false;
    }

    @JsonIgnore
    public List<User> getUserList() {
        //TODO schreiben
        return userList;
    }
}
