package com.Telegram.Processes;

import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class SearchProcess extends Process {

    String searchTerm;
    public SearchProcess(Bot bot, Update update){
        String input = update.getMessage().getText();
        String searchTerm = input.substring(input.indexOf(" ") + 1);
        this.searchTerm = searchTerm;
        setBot(bot);
        performNextStep(searchTerm, update);
    }

    @Override
    public void performNextStep(String arg, Update update) {
        getBot().setBusy(true);
        List<Document> listOfDocs = DBUtil.getDocumentsForSearchTerm(searchTerm);
        LogUtil.log("Send list of Pictures related to \"" + searchTerm);
        BotUtil.sendMsg(update.getMessage().getChatId().toString(), "" + listOfDocs.size() + " Documents found :)", getBot());
        getBot().setBusy(false);
        getBot().process = null;
    }
}
