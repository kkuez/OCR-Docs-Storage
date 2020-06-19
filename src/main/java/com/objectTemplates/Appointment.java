package com.objectTemplates;

import com.backend.taskHandling.Task;
import com.backend.taskHandling.strategies.StrategyType;

import java.time.LocalDateTime;

public class Appointment {

    private LocalDateTime time;

    private String name;

    private long user;

    private Task taskType;

    private StrategyType strategyType;

    private Appointment() {}

    public Appointment(LocalDateTime time, String name, long user, Task taskType, StrategyType strategyType) {
        this.time = time;
        this.name = name;
        this.user = user;
        this.taskType = taskType;
        this.strategyType = strategyType;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public long getUser() {
        return user;
    }

    public Task getTaskType() {
        return taskType;
    }

    public StrategyType getStrategyType() {
        return strategyType;
    }
}
