package com.misc.taskHandling.strategies;

import com.misc.taskHandling.Task;
import com.objectTemplates.User;
import com.telegram.Bot;

import java.util.List;

public class BirdVidTask extends Task {
    public BirdVidTask(List<User> userList, Bot bot, String actionName) {
        super(userList, bot, actionName);
    }

    @Override
    public boolean perform(){
        return true;
    }
}
