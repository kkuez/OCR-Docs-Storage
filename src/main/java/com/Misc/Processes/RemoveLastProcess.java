package com.Misc.Processes;

import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class RemoveLastProcess extends Process {

    public RemoveLastProcess(Bot bot){
    setBot(bot);
    }
    @Override
    public void performNextStep(String arg, Update update) {
        DBUtil.removeLastProcressedDocument();
        BotUtil.sendMsg( update.getMessage().getChatId() + "", "Letztes Bild gelöscht :)",getBot());
        getBot().process = null;
    }
}
