package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class RemoveLastProcess extends Process {

    public RemoveLastProcess(Bot bot, ProgressReporter progressReporter){
        super(progressReporter);
    setBot(bot);
    }
    @Override
    public void performNextStep(String arg, Update update) {
        DBUtil.removeLastProcressedDocument();
        BotUtil.sendMsg( update.getMessage().getChatId() + "", "Letztes Bild gelöscht :)",getBot());
        getBot().getAllowedUsersMap().get(update.getMessage().getFrom().getId()).setProcess(null);
    }

    @Override
    public String getProcessName() {
        return "Remove last Picture";
    }
}
