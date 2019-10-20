package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchProcess extends Process {

    String searchTerm;

    String action;

    String item;
    public SearchProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        performNextStep(searchTerm, update, allowedUsersMap);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        //Terms in this set need more userinformation in a further step
        getBot().setBusy(true);
        Set<String> commandsWithLaterExecution = Set.of("Anzahl Dokumente");
        if(action != null){
            item = update.getMessage().getText();
        }
        if(!commandsWithLaterExecution.contains(update.getMessage().getText())){
            processInOneStep(arg, update, allowedUsersMap);
        }else{
            prepareForProcessing(update);
        }
    }

    private void prepareForProcessing(Update update) {
                Message message = getBot().sendMsg("Wonach soll gesucht werden?", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                getSentMessages().add(message);
                action = "search";
        getBot().setBusy(false);
    }

    private void processInOneStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
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
        getBot().sendMsg("" + listOfDocs.size() + " Dokumente gefunden :)", update, null, true, false);
        getBot().setBusy(false);
        close();
    }

    @Override
    public String getProcessName() {
        return "Search " + searchTerm;
    }
}
