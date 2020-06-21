package com.bot.telegram.processes;

import com.gui.controller.reporter.ProgressReporter;
import com.ObjectHub;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.backend.DBDAO;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class NewUserRegProcess extends Process {

    public NewUserRegProcess(Bot bot, ProgressReporter progressReporter){
        super(progressReporter);
        setBot(bot);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        if(arg.equals(ObjectHub.getInstance().getProperties().getProperty("pwForNewUsers"))){
            getBot().sendMsg("Willkommen :)", update, null, true, false);
            DBDAO.executeSQL("insert into AllowedUsers(id, name, chatId) Values (" + update.getMessage().getFrom().getId() + ", '" +
                    update.getMessage().getFrom().getFirstName() + "', " + update.getMessage().getChatId() + ")");
            ObjectHub.getInstance().setAllowedUsersMap(DBDAO.getAllowedUsersMap());
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
