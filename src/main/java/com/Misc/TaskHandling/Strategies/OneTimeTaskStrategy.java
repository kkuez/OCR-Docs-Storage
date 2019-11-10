package com.Misc.TaskHandling.Strategies;

import com.Misc.TaskHandling.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class OneTimeTaskStrategy implements TaskStrategy {

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
