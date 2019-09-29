package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Set;

public class SearchProcess extends Process {

    String searchTerm;

    String action;

    String item;
    public SearchProcess(Bot bot, Update update, ProgressReporter progressReporter){
        super(progressReporter);

        setBot(bot);
        performNextStep(searchTerm, update);
    }

    @Override
    public void performNextStep(String arg, Update update) {
        //Terms in this set need more userinformation in a further step
        getBot().setBusy(true);
        Set<String> commandsWithLaterExecution = Set.of("Anzahl Dokumente");
        if(action != null){
            item = update.getMessage().getText();
        }

        if(!commandsWithLaterExecution.contains(update.getMessage().getText())){
            processInOneStep(arg, update);
        }else{
            prepareForProcessing(update);
        }
    }

    private void prepareForProcessing(Update update) {
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Wonach soll gesucht werden?", getBot());
                action = "search";
        getBot().setBusy(false);
    }

    private void processInOneStep(String arg, Update update) {
        List<Document> listOfDocs;
        if(action != null){
            this.searchTerm = item;
                    listOfDocs = DBUtil.getDocumentsForSearchTerm(item);
        }else{
            String input = update.getMessage().getText();
            this.searchTerm = input.substring(input.indexOf(" ") + 1);;
            listOfDocs = DBUtil.getDocumentsForSearchTerm(searchTerm);
        }
        LogUtil.log("Send list of Pictures related to \"" + searchTerm);
        BotUtil.sendMsg(update.getMessage().getChatId().toString(), "" + listOfDocs.size() + " Dokumente gefunden :)", getBot());
        getBot().setBusy(false);
        getBot().process = null;
    }

    @Override
    public String getProcessName() {
        return "Search " + searchTerm;
    }
}
