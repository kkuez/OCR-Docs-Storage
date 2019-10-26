package com;

import com.Misc.TaskHandling.Strategies.RegularTaskStrategy;
import com.Misc.TaskHandling.Task;
import com.Utils.LogUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Taskshub implements Runnable {

    private List<Task> tasksToDo = new ArrayList<>();

    private boolean loopActive;

    private int currentMinute;

    private int currentHour;

    private String currentDate;

    private void loop() {
        loopActive = true;
        while (loopActive) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            refreshTimes();

            List<Task> toBeRemovedList = new ArrayList<>();
            for (Task task : tasksToDo) {
             boolean success = task.perform();
             //if successfully performed and is NOT a regular task, remove from list
             if(success){
                 StringBuilder usersString = new StringBuilder();
                 task.getUserList().forEach(user -> usersString.append(", " + user.getName()));
                 LogUtil.log("Task " + task.getName() + " for user " + usersString.toString().replaceFirst(", ", ""));
                 if(!(task.getTaskStrategy() instanceof RegularTaskStrategy)) {
                     toBeRemovedList.add(task);
                 }
             }
            }
            tasksToDo.removeAll(toBeRemovedList);
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
