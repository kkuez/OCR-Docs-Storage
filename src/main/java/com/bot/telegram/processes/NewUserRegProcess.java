package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.reporter.ProgressReporter;
import org.telegram.telegrambots.meta.api.objects.Update;

public class NewUserRegProcess extends Process {

    public NewUserRegProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        if (arg.equals(getFacade().getProperties().getProperty("pwForNewUsers"))) {
            bot.sendMsg("Willkommen :)", update, null, true, false);
            getFacade().insertUserToAllowedUsers(update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName(), update.getMessage().getChatId());
        } else {
            bot.getAllowedUsersMap().remove(update.getMessage().getFrom().getId());
        }
    }

    @Override
    public String getProcessName() {
        return "New User Registration";
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        return null;
    }

    @Override
    public boolean hasCommand(String cmd) {
        // TODO
        return false;
    }
}
