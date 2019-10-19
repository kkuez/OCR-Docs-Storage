package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.google.inject.internal.cglib.core.$DuplicatesPredicate;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class RemoveLastProcess extends Process {

    public RemoveLastProcess(Bot bot, ProgressReporter progressReporter, Update update, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
    setBot(bot);
    performNextStep("asd", update, allowedUsersMap);
    }
    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        DBUtil.removeLastProcressedDocument();
        BotUtil.sendMsg( "Letztes Bild gelöscht :)", getBot(), update, null, true, false);
        close();
    }

    @Override
    public String getProcessName() {
        return "Remove last Picture";
    }
}
