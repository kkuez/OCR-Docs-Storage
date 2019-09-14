package com.Misc.Processes;

import com.ObjectHub;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class NewUserRegProcess extends Process {
    @Override
    public void performNextStep(String arg, Update update) {
        if(arg.equals(ObjectHub.getInstance().getProperties().getProperty("pwForNewUsers"))){
            BotUtil.sendMsg(update.getMessage().getChatId() + "", "Willkommen :)", Bot.bot);
            DBUtil.executeSQL("insert into AllowedUsers(id, name) Values (" + update.getMessage().getFrom().getId() + ", '" +
                    update.getMessage().getFrom().getFirstName() + "')");
            ObjectHub.getInstance().setAllowedUsersMap(DBUtil.getAllowedUsersMap());
            Bot.process = null;
        }
    }
}
