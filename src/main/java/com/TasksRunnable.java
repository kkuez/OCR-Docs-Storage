package com;

import com.misc.taskHandling.strategies.RegularTaskStrategy;
import com.misc.taskHandling.Task;
import com.telegram.Bot;
import com.utils.DBUtil;

import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;

public class TasksRunnable implements Runnable {

    private static Logger logger = Main.getLogger();

    private List<Task> tasksToDo = new ArrayList<>();

    private boolean loopActive;

    private Bot bot;

    private void loop() throws InterruptedException {
        loopActive = true;
        while (loopActive) {
            tasksToDo = DBUtil.getTasksFromDB(bot);
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
                        if(!(task.getTaskStrategy() instanceof RegularTaskStrategy)) {
                            task.delete();
                            DBUtil.removeTask(task);
                        } else {
                            //TODO der user wird NICHT benachrichtigt, auch das Keyboard mit den Items verändert sich nicht!!
                            logger.info("Deletion of regular tasks not supported yet :(");
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
