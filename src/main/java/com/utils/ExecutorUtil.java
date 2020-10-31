package com.utils;

import com.backend.ObjectHub;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorUtil {

    private ExecutorUtil() {}

    public static void blockUntilExecutorIsDone(ObjectHub objectHub, int tasksToFinish) throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) objectHub.getExecutorService();

        boolean isDone = threadPoolExecutor.getCompletedTaskCount() == tasksToFinish;

        while (!isDone) {
            isDone = threadPoolExecutor.getCompletedTaskCount() == tasksToFinish;
            Thread.sleep(500);
        }
        resetExecutor(objectHub);
    }

    public static void resetExecutor(ObjectHub objectHub){
        objectHub.setExecutorService(Executors.newFixedThreadPool(Integer.parseInt(objectHub.getProperties().getProperty("threads"))));
    }
}
