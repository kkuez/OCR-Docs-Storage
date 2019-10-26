package com.Misc.TaskHandling.Strategies;

import com.Misc.TaskHandling.Task;
import com.ObjectTemplates.User;
import com.Telegram.KeyboardFactory;

import java.time.LocalDateTime;

public class SimpleCalendarOneTimeStrategy extends OneTimeTaskStrategy {

    Task task;

    public SimpleCalendarOneTimeStrategy(Task task){
        this.task = task;
    }

    @Override
    public boolean perform() {
        for(User user : task.getUserList()){
            String userName = user.getName();
            task.getBot().sendSimpleMsg("Hey " + userName + ",\n " + task.getName(), user.getId(), KeyboardFactory.KeyBoardType.NoButtons, true);
        }
    return true;
    }


    @Override
    public String getType() {
        return "SimpleCalendarOneTimeStrategy";
    }

    @Override
    public String getStrategyName() {
        return "Einmaliger Termin";
    }
}
