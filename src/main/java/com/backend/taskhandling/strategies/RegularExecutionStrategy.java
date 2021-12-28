package com.backend.taskhandling.strategies;

import com.backend.taskhandling.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public abstract class RegularExecutionStrategy implements ExecutionStrategy {
    @JsonIgnore
    Task task;

    @JsonIgnore
    public Logger logger = LoggerFactory.getLogger(RegularExecutionStrategy.class);

    private LocalDateTime time;

    public RegularExecutionStrategy(LocalDateTime time) {
        this.time = time;
    }

    public abstract TimeUnit getExecutionTimeUnit();

    public void delete(String taskName) {
        // TODO
    };

    // GETTER SETTER

    public Task getTask() {
        return task;
    }

    @Override
    public LocalDateTime getTime() {
        return time;
    }
}
