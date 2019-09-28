package com.Misc.TaskHandling;

import com.Telegram.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class UpdateTask implements Task {
    Update update;

    Bot bot;

    boolean successFullyExecuted;


    public UpdateTask(Update update, Bot bot){
        this.update = update;
        this.bot = bot;
        successFullyExecuted = false;
    }

    @Override
    public void run() {
        try {
            bot.processUpdateReceveived(update);
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
