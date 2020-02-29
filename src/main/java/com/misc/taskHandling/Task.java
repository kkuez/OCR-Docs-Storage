package com.misc.taskHandling;

import com.Main;
import com.misc.taskHandling.strategies.OneTimeTaskStrategy;
import com.misc.taskHandling.strategies.TaskStrategy;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.telegram.KeyboardFactory;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task implements Comparable{

    private static Logger logger = Main.getLogger();

    private TaskStrategy taskStrategy;

    private String name;

    private Bot bot;

    private List<User> userList = new ArrayList<>();

    public Task(Bot bot){
        this.bot = bot;
    }
    public Task(List<User> userList, Bot bot, String actionName){
        this.userList = userList;
        this.bot = bot;
        this.name = actionName;
    }

    public boolean perform(){
        for(User user : getUserList()){
            String userName = user.getName();
            getBot().sendSimpleMsg("Hey " + userName + ",\n " + getName(), user.getId(), KeyboardFactory.KeyBoardType.NoButtons, true);
        }
        return true;
    }

    public boolean timeIsNow(LocalDateTime localDateTime) {
        return taskStrategy.timeIsNow(localDateTime);
    }

    public String getInsertDBString(){
        return taskStrategy.getInsertDBString();
   }

    //GETTER SETTER

    public TaskStrategy getTaskStrategy() {
        return taskStrategy;
    }

    public void setTaskStrategy(TaskStrategy taskStrategy) {
        this.taskStrategy = taskStrategy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    Task task;

    @Override
    public int compareTo(Object o) throws RuntimeException{
        if(!o.getClass().equals(this.getClass())){
            throw  new RuntimeException("Classes dont match!");
        }
        if(!(this.getTaskStrategy() instanceof OneTimeTaskStrategy) || !(((Task)o).getTaskStrategy() instanceof OneTimeTaskStrategy)){
            return 1;
        }

        Task oTask = (Task) o;
        return getTaskStrategy().getTime().compareTo(oTask.getTaskStrategy().getTime());
    }

    public void delete() {
        taskStrategy.delete(getName());
    }
}
