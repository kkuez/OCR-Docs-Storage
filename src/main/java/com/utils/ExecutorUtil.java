package com.utils;

import com.Main;
import com.ObjectHub;
import org.apache.log4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorUtil {

    private static Logger logger = Main.logger;

    public static void blockUntilExecutorIsDone(Executor executor, int tasksToFinish) throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;

        boolean isDone = threadPoolExecutor.getCompletedTaskCount() == tasksToFinish;

        while (!isDone) {
            isDone = threadPoolExecutor.getCompletedTaskCount() == tasksToFinish;
            Thread.sleep(500);
        }
        resetExecutor();
    }

    public static void resetExecutor(){
        ObjectHub.getInstance().setExecutorService(Executors.newFixedThreadPool(Integer.parseInt(ObjectHub.getInstance().getProperties().getProperty("threads"))));
    }
}
