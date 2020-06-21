package com;

import com.backend.taskHandling.CheckConnectionTask;
import com.backend.taskHandling.strategies.RegularMinutelyExecutionStrategy;
import com.backend.taskHandling.strategies.RegularExecutionStrategy;
import com.backend.taskHandling.Task;
import com.bot.telegram.Bot;
import com.backend.DBDAO;

import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;

public class TasksRunnable implements Runnable {

    private static Logger logger = Main.getLogger();

    private List<Task> tasksToDo = new ArrayList<>();

    private boolean loopActive;

    private Bot bot;

    private CheckConnectionTask checkConnectionTask;

    private void loop() throws InterruptedException {
        loopActive = true;

        while (loopActive) {
            tasksToDo = DBDAO.getTasksFromDB();
            tasksToDo.add(checkConnectionTask);
            LocalDateTime localDateTimeNow;
            for (Task task : tasksToDo) {
                localDateTimeNow = LocalDateTime.now().withSecond(0).withNano(0);
                if(task.timeIsNow(localDateTimeNow)){
                    boolean success = task.perform();
                    //if successfully performed and is NOT a regular task, remove from list
                    if(success){
                        StringBuilder usersString = new StringBuilder();
                        task.getUserList().forEach(user -> usersString.append(", " + user.getName()));
                        logger.info("Task " + task.getName() + " for user " + usersString.toString().replaceFirst(", ", ""));
                        if(!(task.getExecutionStrategy() instanceof RegularExecutionStrategy)) {
                            task.delete();
                            DBDAO.removeTask(task);
                        }
                    }
                }
            }
                Thread.sleep(60000);
        }
    }

    @Override
    public void run() {
        try {
            checkConnectionTask = new CheckConnectionTask(bot);
            RegularMinutelyExecutionStrategy checkConnectionTaskStrategy = new RegularMinutelyExecutionStrategy(checkConnectionTask);
            checkConnectionTask.setExecutionStrategy(checkConnectionTaskStrategy);
            loop();
        } catch (InterruptedException e) {
            logger.error("Couldn't run CalendarTasks.", e);
            Thread.currentThread().interrupt();
            System.exit(2);
        }
    }

    //GETTER SETTER


    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public boolean isLoopActive() {
        return loopActive;
    }

    public void setLoopActive(boolean loopActive) {
        this.loopActive = loopActive;
    }

    public List<Task> getTasksToDo() {
        return tasksToDo;
    }

    public void setTasksToDo(List<Task> tasksToDo) {
        this.tasksToDo = tasksToDo;
    }
}
