package com.Utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorUtil {

    public static void blockUntilExecutorIsDone(Executor executor) {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;

        boolean isDone = threadPoolExecutor.getQueue().size() < 1;

        while (!isDone) {
            isDone = threadPoolExecutor.getQueue().size() < 1;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
