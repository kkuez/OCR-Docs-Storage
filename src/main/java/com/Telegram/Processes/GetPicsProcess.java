package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class GetPicsProcess extends Process {

    String searchTerm;
    public GetPicsProcess(Bot bot, Update update, ProgressReporter progressReporter){
        super(progressReporter);
        BotUtil.sendMsg(update.getMessage().getChatId() + "", "Hole Bilder...", bot);
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
        listOfDocs.forEach(document -> getBot().sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath(), "", null));
        getBot().setBusy(false);
        BotUtil.sendMsg(update.getMessage().getChatId() + "", "Fertig: " + listOfDocs.size() + " Bilder geholt.", getBot());
        getBot().process = null;
    }

    @Override
    public String getProcessName() {
        return "Get-Pics " + searchTerm;
    }
}
