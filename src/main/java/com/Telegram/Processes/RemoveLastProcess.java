package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class RemoveLastProcess extends Process {

    public RemoveLastProcess(Bot bot, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
    setBot(bot);
    }
    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        DBUtil.removeLastProcressedDocument();
        BotUtil.sendMsg( update.getMessage().getChatId() + "", "Letztes Bild gelöscht :)",getBot());
        allowedUsersMap.get(update.getMessage().getFrom().getId()).setProcess(null);
    }

    @Override
    public String getProcessName() {
        return "Remove last Picture";
    }
}
