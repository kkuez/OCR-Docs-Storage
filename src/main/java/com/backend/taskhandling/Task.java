package com.backend.taskhandling;

import com.backend.taskhandling.strategies.ExecutionStrategy;
import com.backend.taskhandling.strategies.OneTimeExecutionStrategy;
import com.backend.taskhandling.strategies.StrategyType;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectTemplates.User;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Task implements Comparable {

    private ExecutionStrategy executionStrategy;

    private String name;

    //@JsonSerialize(using = UUIDToString.class, as=String.class)
    private UUID eID;

    @JsonIgnore
    private Bot bot;

    private List<Integer> userList = new ArrayList<>();

    public Task(){}

    public Task(Bot bot) {
        this.bot = bot;
    }

    public Task(List<User> userList, Bot bot, String actionName, UUID eID) {
        this.userList = userList.stream().map(User::getId).collect(Collectors.toList());
        this.bot = bot;
        this.name = actionName;
        this.eID = eID;
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

    // GETTER SETTER
    public StrategyType getType() {
        return executionStrategy.getType();
    }

    public String getTimeString() {
        return executionStrategy.getTime().toString();
    }

    @JsonIgnore
    public String getInsertDBString() {
        return executionStrategy.getInsertDBString();
    }

    @JsonIgnore
    public ExecutionStrategy getExecutionStrategy() {
        return executionStrategy;
    }

    public void setExecutionStrategy(ExecutionStrategy executionStrategy) {
        this.executionStrategy = executionStrategy;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    public String getNameString() {
        return name.replace(" ", "%x20");
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    @JsonIgnore
    public List<User> getUserList() {
        return userList.stream().filter(bot.getAllowedUsersMap()::containsKey)
                .map(bot.getAllowedUsersMap()::get).collect(Collectors.toList());
    }

    public UUID geteID() {
        return eID;
    }
}
