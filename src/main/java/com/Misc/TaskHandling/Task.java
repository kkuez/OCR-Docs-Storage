package com.Misc.TaskHandling;

import com.Misc.TaskHandling.Strategies.TaskStrategy;
import com.ObjectTemplates.User;
import com.Telegram.Bot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {

    private TaskStrategy taskStrategy;

    private String name;

    private Bot bot;

    private List<User> userList = new ArrayList<>();

    public Task(User user, Bot bot){}
    public Task(List<User> userList, Bot bot, String actionName){
        this.userList = userList;
        this.bot = bot;
        this.name = actionName;
    }

    public boolean perform(){
       return taskStrategy.perform();
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
}
