package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.backend.ObjectHub;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class NewUserRegProcess extends Process {

    public NewUserRegProcess(Bot bot, ProgressReporter progressReporter, BackendFacade facade){
        super(progressReporter, facade);
        setBot(bot);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        if(arg.equals(ObjectHub.getInstance().getProperties().getProperty("pwForNewUsers"))){
            getBot().sendMsg("Willkommen :)", update, null, true, false);
            getFacade().insertUserToAllowedUsers(update.getMessage().getFrom().getId(), update.getMessage().getFrom().getFirstName(), update.getMessage().getChatId());
            ObjectHub.getInstance().setAllowedUsersMap(getFacade().getAllowedUsers());
            setDeleteLater(true);
        }else{
            allowedUsersMap.remove(update.getMessage().getFrom().getId());
        }
    }

    @Override
    public String getProcessName() {
        return "New User Registration";
    }

    @Override
    public String getCommandIfPossible(Update update) {
        return null;
    }
}
