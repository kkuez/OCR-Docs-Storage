package com.backend.taskhandling.strategies;

import com.backend.BackendFacade;
import com.backend.taskhandling.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OneTimeExecutionStrategy implements ExecutionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(OneTimeExecutionStrategy.class);

    @JsonIgnore
    private final BackendFacade facade;

    private String name;

    @JsonIgnore
    private final Task task;

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
