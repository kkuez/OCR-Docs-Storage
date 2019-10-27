package com;

import com.Misc.TaskHandling.Strategies.RegularTaskStrategy;
import com.Misc.TaskHandling.Task;
import com.Telegram.Bot;
import com.Utils.DBUtil;
import com.Utils.LogUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TasksRunnable implements Runnable {

    private List<Task> tasksToDo = new ArrayList<>();

    private boolean loopActive;

    private int currentMinute;

    private int currentHour;

    private String currentDate;

    private Bot bot;

    private void loop() {
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
                        LogUtil.log("Task " + task.getName() + " for user " + usersString.toString().replaceFirst(", ", ""));
                        if(!(task.getTaskStrategy() instanceof RegularTaskStrategy)) {
                            DBUtil.removeTask(task);
                        }
                    }
                }
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        loop();
    }

    private void refreshTimes(){
        LocalDateTime localDateTime = LocalDateTime.now();
        currentDate = LocalDate.now().toString();
        currentHour = localDateTime.getHour();
        currentMinute = localDateTime.getMinute();
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
