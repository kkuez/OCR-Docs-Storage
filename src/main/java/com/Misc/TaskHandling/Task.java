package com.Misc.TaskHandling;

import com.Misc.TaskHandling.Strategies.TaskStrategy;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.DBUtil;
import com.Utils.IOUtil;
import org.apache.commons.io.FileUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {

    private LocalDateTime time;

    private TaskStrategy taskStrategy;

    private String name;

    private Bot bot;

    private List<User> userList = new ArrayList<>();

    public Task(User user, Bot bot){}

    public void perform(){
        taskStrategy.perform();
    }

    public boolean timeIsNow(LocalDateTime localDateTime) {
        return time.equals(localDateTime);
    }

    public String getInsertDBString(){
         int year = time.getYear();

         int month = time.getMonth().getValue();

         int day = time.getDayOfMonth();

         int hour = time.getHour();

         int minute = time.getMinute();

        String user = userList.size() > 0 ? "ALL" : userList.get(0).getName();

        return "insert into Documents (year, month, day, hour, minute, name, user) Values (" + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", '" + name + "', '" + user + "')";
    }

    //GETTER SETTER

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

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
