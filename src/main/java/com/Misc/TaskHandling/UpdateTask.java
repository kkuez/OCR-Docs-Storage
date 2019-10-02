package com.Misc.TaskHandling;

import com.Misc.TaskHandling.Strategies.TaskStrategy;
import com.ObjectHub;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class UpdateTask implements Task {
    Update update;

    Bot bot;

    boolean successFullyExecuted;

    TaskStrategy taskStrategy;

    public UpdateTask(Update update, Bot bot, TaskStrategy taskStrategy){
        this.update = update;
        this.bot = bot;
        successFullyExecuted = false;
        this.taskStrategy = taskStrategy;
    }

    @Override
    public TaskStrategy getTaskStrategy() {
        return taskStrategy;
    }

    @Override
    public void run() {
        try {
            bot.processUpdateReceveived(update, ObjectHub.getInstance().getAllowedUsersMap());
            successFullyExecuted = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFromList(List<Task> taskList) {
        taskList.remove(this);
    }


    //GETTER SETTER
    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public boolean isSuccessFullyExecuted() {
        return successFullyExecuted;
    }

    public void setSuccessFullyExecuted(boolean successFullyExecuted) {
        this.successFullyExecuted = successFullyExecuted;
    }
}
