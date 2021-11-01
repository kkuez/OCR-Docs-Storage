package com.backend.taskhandling.strategies;

import com.StartUp;
import com.backend.taskhandling.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public abstract class RegularExecutionStrategy implements ExecutionStrategy {
    @JsonIgnore
    Task task;

    @JsonIgnore
    public Logger logger = StartUp.createLogger(RegularExecutionStrategy.class);

    private LocalDateTime time;

    public RegularExecutionStrategy(LocalDateTime time) {
        this.time = time;
    }

    public abstract TimeUnit getExecutionTimeUnit();

    public void delete(String taskName) {
        // TODO
        Logger.getLogger("Deletion of regular tasks not supported... yet :)");
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
