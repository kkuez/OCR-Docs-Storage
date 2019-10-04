package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.BotUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class StartProcess extends Process {

    public StartProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        BotUtil.sendKeyBoard("Wähle eine Aktion :)", bot, update, KeyboardFactory.KeyBoardType.Start);
        setDeleteLater(true);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {

    }

    @Override
    public String getProcessName() {
        return "Start";
    }
}
