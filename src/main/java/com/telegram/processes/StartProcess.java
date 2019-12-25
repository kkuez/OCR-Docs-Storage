package com.telegram.processes;

import com.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.telegram.KeyboardFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class StartProcess extends Process {

    public StartProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        getBot().sendMsg("Wähle eine Aktion:", update, KeyboardFactory.KeyBoardType.Start, true, false);
        close();
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {

    }

    @Override
    public String getProcessName() {
        return "Start";
    }

    @Override
    public String getCommandIfPossible(Update update) {
        return null;
    }
}