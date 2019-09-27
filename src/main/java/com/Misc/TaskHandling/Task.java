package com.Misc.TaskHandling;

public interface Task extends Runnable {

    public void run();
    boolean isSuccessFullyExecuted();

}
