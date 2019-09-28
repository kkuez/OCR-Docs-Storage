package com.Misc.TaskHandling;

import com.Misc.TaskHandling.Strategies.TaskStrategy;

import java.util.List;

public interface Task extends Runnable {

    TaskStrategy getTaskStrategy();

    public void run();

    void deleteFromList(List<Task> taskList);

    boolean isSuccessFullyExecuted();

}
