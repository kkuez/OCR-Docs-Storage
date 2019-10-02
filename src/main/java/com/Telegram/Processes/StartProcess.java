package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.BotUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StartProcess extends Process {

    public StartProcess(Bot bot, Update update, ProgressReporter progressReporter){
        super(progressReporter);
        BotUtil.sendKeyBoard("Wähle eine Aktion :)",bot, update, KeyboardFactory.KeyBoardType.Start);
        getBot().getAllowedUsersMap().get(update.getMessage().getFrom().getId()).setProcess(null);
    }

    @Override
    public void performNextStep(String arg, Update update) {

    }

    @Override
    public String getProcessName() {
        return "Start";
    }
}
