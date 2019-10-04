package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
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
            BotUtil.sendMsg("Willkommen :)", getBot(), update.getMessage(), null, true, false);
            DBUtil.executeSQL("insert into AllowedUsers(id, name, chatId) Values (" + update.getMessage().getFrom().getId() + ", '" +
                    update.getMessage().getFrom().getFirstName() + "', " + update.getMessage().getChatId() + ")");
            ObjectHub.getInstance().setAllowedUsersMap(DBUtil.getAllowedUsersMap());
            setDeleteLater(true);
        }else{
            allowedUsersMap.remove(update.getMessage().getFrom().getId());
        }
    }

    @Override
    public String getProcessName() {
        return "New User Registration";
    }
}
