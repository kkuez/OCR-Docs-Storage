package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.IOUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GetPicsProcess extends Process {

    String searchTerm;

    String action;

    String item;
    public GetPicsProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        performNextStep(searchTerm, update, allowedUsersMap);
    }
    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        getBot().setBusy(true);
        Set<String> commandsWithLaterExecution = Set.of("Hole Bilder, Dokumente");
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
        BotUtil.sendMsg(update.getMessage().getChatId() + "", "Wonach soll gesucht werden?", getBot());
        action = "getpics";
        getBot().setBusy(false);
    }

    private void processInOneStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                List<Document> listOfDocs;
                if(action != null){
                    searchTerm = item;
                    listOfDocs = DBUtil.getDocumentsForSearchTerm(item);
                }else{
                    String input = update.getMessage().getText();
                    searchTerm = input.substring(input.indexOf(" ") + 1);;
                    listOfDocs = DBUtil.getDocumentsForSearchTerm(searchTerm);
                }
                listOfDocs.forEach(document -> {

                    //In case that a wrong path is given in the db f.e. when pictures not added on the local system, but on
                    //a remote one, this will be catched.
                    if(!document.getOriginFile().exists()){
                        return;
                    }
                    if(document.getOriginalFileName().toLowerCase().endsWith("pdf")){
                        getBot().sendDocumentFromURL(update, document.getOriginFile().getPath(), document.getOriginalFileName(), null);
                    }else{
                        getBot().sendPhotoFromURL(update, document.getOriginFile().getPath(), "", null);
                    }
                });
                getBot().setBusy(false);
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Fertig: " + listOfDocs.size() + " Bilder geholt.", getBot());
            }
        });
        thread.start();
        setDeleteLater(true);
    }

    @Override
    public String getProcessName() {
        return "Get-Pics " + searchTerm;
    }
}
