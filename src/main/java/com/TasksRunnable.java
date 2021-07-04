package com;

import com.backend.BackendFacade;
import com.backend.ObjectHub;
import com.backend.taskhandling.CheckConnectionTask;
import com.backend.taskhandling.Task;
import com.backend.taskhandling.strategies.RegularExecutionStrategy;
import com.backend.taskhandling.strategies.RegularMinutelyExecutionStrategy;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TasksRunnable implements Runnable {

    private static Logger logger = StartUp.createLogger(TasksRunnable.class);

    private List<Task> tasksToDo = new ArrayList<>();

    private boolean loopActive;

    private CheckConnectionTask checkConnectionTask;

    private BackendFacade facade;
    private ObjectHub objectHub;

    private void loop() throws InterruptedException {
        loopActive = true;

        while (loopActive) {
            tasksToDo = facade.getTasks();
            tasksToDo.add(checkConnectionTask);
            LocalDateTime localDateTimeNow;
            for (Task task : tasksToDo) {
                localDateTimeNow = LocalDateTime.now().withSecond(0).withNano(0);
                if (task.timeIsNow(localDateTimeNow)) {
                    boolean success = task.perform();
                    // if successfully performed and is NOT a regular task, remove from list
                    if (success) {
                        StringBuilder usersString = new StringBuilder();
                        task.getUserList().forEach(user -> usersString.append(", " +
                                facade.getAllowedUsers().get(user).getName()));
                        logger.info("Task " + task.getName() + " for user "
                                + usersString.toString().replaceFirst(", ", ""));
                        if (!(task.getExecutionStrategy() instanceof RegularExecutionStrategy)) {
                            task.delete();
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
            checkConnectionTask = new CheckConnectionTask(objectHub);
            RegularMinutelyExecutionStrategy checkConnectionTaskStrategy = new RegularMinutelyExecutionStrategy(
                    checkConnectionTask);
            checkConnectionTask.setExecutionStrategy(checkConnectionTaskStrategy);
            loop();
        } catch (InterruptedException e) {
            logger.error("Couldn't run CalendarTasks.", e);
            Thread.currentThread().interrupt();
            System.exit(2);
        }
    }

    // GETTER SETTER
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

    @Autowired
    public void setObjectHub(ObjectHub objectHub) {
        this.objectHub = objectHub;
    }

    @Autowired
    public void setFacade(BackendFacade facade) {
        this.facade = facade;
    }
}
