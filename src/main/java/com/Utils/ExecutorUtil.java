package com.Utils;

import com.ObjectHub;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorUtil {

    public static void blockUntilExecutorIsDone(Executor executor, int tasksToFinish) {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;

        boolean isDone = threadPoolExecutor.getCompletedTaskCount() == tasksToFinish;

        while (!isDone) {
            isDone = threadPoolExecutor.getCompletedTaskCount() == tasksToFinish;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        resetExecutor();
    }

    public static void resetExecutor(){
        ObjectHub.getInstance().setExecutorService(Executors.newFixedThreadPool(Integer.parseInt(ObjectHub.getInstance().getProperties().getProperty("threads"))));
    }
}
