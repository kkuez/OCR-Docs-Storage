package com;

import com.Misc.TaskHandling.Task;
import com.Utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class Taskshub implements Runnable {

    private List<Task> tasksToDo = new ArrayList<>();

    private int performTimesPerHour;

    private boolean loopActive;

    public Taskshub(int performTimesPerHour){
        this.performTimesPerHour = performTimesPerHour;
    }

    private void loop() {
        loopActive = true;
        while (loopActive){
            try {
                Thread.sleep(60000 / performTimesPerHour);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogUtil.log("System: Trying to perform LaterTasks (" + tasksToDo.size() + " in List)");
           for(Task task : tasksToDo){
               task.run();
               if(task.isSuccessFullyExecuted()){
                   task.deleteFromList(tasksToDo);
               }
           }
        }
    }

    @Override
    public void run() {
        loop();
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

}
