package com.backend.taskhandling;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.Main;
import com.backend.taskhandling.strategies.ExecutionStrategy;
import com.backend.taskhandling.strategies.OneTimeExecutionStrategy;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.objectTemplates.User;

public class Task implements Comparable {

    private static Logger logger = Main.getLogger();

    private ExecutionStrategy executionStrategy;

    private String name;

    private Bot bot;

    private List<User> userList = new ArrayList<>();

    public Task(Bot bot) {
        this.bot = bot;
    }

    public Task(List<User> userList, Bot bot, String actionName) {
        this.userList = userList;
        this.bot = bot;
        this.name = actionName;
    }

    public boolean perform() {
        Message message = null;
        for (User user : getUserList()) {
            String userName = user.getName();
            message = getBot().sendSimpleMsg("Hey " + userName + ",\n " + getName(), user.getId(),
                    KeyboardFactory.KeyBoardType.NoButtons, true, null);
        }
        return message != null;
    }

    public boolean timeIsNow(LocalDateTime localDateTime) {
        return executionStrategy.timeIsNow(localDateTime);
    }

    public String getInsertDBString() {
        return executionStrategy.getInsertDBString();
    }

    // GETTER SETTER

    public ExecutionStrategy getExecutionStrategy() {
        return executionStrategy;
    }

    public void setExecutionStrategy(ExecutionStrategy executionStrategy) {
        this.executionStrategy = executionStrategy;
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
    public int compareTo(Object o) throws RuntimeException {
        if (!o.getClass().equals(this.getClass())) {
            throw new RuntimeException("Classes dont match!");
        }
        if (!(this.getExecutionStrategy() instanceof OneTimeExecutionStrategy)
                || !(((Task) o).getExecutionStrategy() instanceof OneTimeExecutionStrategy)) {
            return 1;
        }

        Task oTask = (Task) o;
        return getExecutionStrategy().getTime().compareTo(oTask.getExecutionStrategy().getTime());
    }

    public void delete() {
        executionStrategy.delete(getName());
    }
}
