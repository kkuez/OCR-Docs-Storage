package com.misc.taskHandling.strategies;

import com.Main;
import com.misc.taskHandling.Task;
import org.apache.log4j.Logger;

public abstract class OneTimeTaskStrategy implements TaskStrategy {
    private static Logger logger = Main.getLogger();

    private String name;

    Task task;

    public OneTimeTaskStrategy(){}

    @Override
    public String getType() {
        return "OneTimeTaskStrategy";
    }

    //GETTER SETTER

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
