package com.telegram.processes;

import com.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.utils.DBUtil;
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
        getBot().sendMsg( "Letztes Bild gelöscht :)", update, null, true, false);
        close();
    }

    @Override
    public String getProcessName() {
        return "Remove last Picture";
    }

    @Override
    public String getCommandIfPossible(Update update) {
        return null;
    }
}
