package com.backend.taskHandling.strategies;

import com.Main;
import com.backend.BackendFacade;
import com.backend.taskHandling.Task;
import org.apache.log4j.Logger;

public abstract class OneTimeExecutionStrategy implements ExecutionStrategy {
    private static Logger logger = Main.getLogger();

    private final BackendFacade facade;

    private String name;

    Task task;

    public OneTimeExecutionStrategy(BackendFacade facade){
        this.facade = facade;
    }

    @Override
    public StrategyType getType() {
        return StrategyType.ONETIME;
    }

    public void delete(String taskName){
        facade.deleteTask(task);
    }

    //GETTER SETTER

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
