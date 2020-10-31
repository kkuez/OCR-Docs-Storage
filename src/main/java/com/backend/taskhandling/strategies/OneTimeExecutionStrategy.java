package com.backend.taskhandling.strategies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.log4j.Logger;

import com.StartUp;
import com.backend.BackendFacade;
import com.backend.taskhandling.Task;

public abstract class OneTimeExecutionStrategy implements ExecutionStrategy {

    private static Logger logger = StartUp.getLogger();

    @JsonIgnore
    private final BackendFacade facade;

    private String name;

    @JsonIgnore
    private Task task;

    public OneTimeExecutionStrategy(BackendFacade facade, Task task) {
        this.task = task;
        this.facade = facade;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.ONETIME;
    }

    public void delete(String taskName) {
        facade.deleteTask(task);
    }

    // GETTER SETTER

    @JsonIgnore
    public Task getTask() {
        return task;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
