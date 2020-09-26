package com.backend.taskhandling.strategies;

import org.apache.log4j.Logger;

import com.Main;
import com.backend.BackendFacade;
import com.backend.taskhandling.Task;

public abstract class OneTimeExecutionStrategy implements ExecutionStrategy {

    private static Logger logger = Main.getLogger();

    private final BackendFacade facade;

    private String name;

    private Task task;

    public OneTimeExecutionStrategy(BackendFacade facade) {
        this.facade = facade;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.ONETIME;
    }

    public void delete(String taskName) {
        // TODO input parameter wird nicht genutzt
        facade.deleteTask(task);
    }

    // GETTER SETTER

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
