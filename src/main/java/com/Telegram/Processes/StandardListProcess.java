package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class StandardListProcess extends Process {

    public StandardListProcess(ProgressReporter progressReporter, Bot bot, Update update, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        this.setBot(bot);
        getBot().setBusy(true);
        performNextStep("-", update,  allowedUsersMap);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {

    }

    @Override
    public String getProcessName() {
        return null;
    }

    @Override
    public String getCommandIfPossible(Update update) {
        return null;
    }

    //GETTER SETTER
}
