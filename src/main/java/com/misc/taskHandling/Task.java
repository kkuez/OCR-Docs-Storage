package com.misc.taskHandling;

import com.misc.taskHandling.strategies.TaskStrategy;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.telegram.KeyboardFactory;

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
}
