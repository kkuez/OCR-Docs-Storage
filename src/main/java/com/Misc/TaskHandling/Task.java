package com.Misc.TaskHandling;

import java.util.List;

public interface Task extends Runnable {

    public void run();

    void deleteFromList(List<Task> taskList);

    boolean isSuccessFullyExecuted();

}
