package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.IOUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Set;

public class GetPicsProcess extends Process {

    String searchTerm;

    String action;

    String item;
    public GetPicsProcess(Bot bot, Update update, ProgressReporter progressReporter){
        super(progressReporter);
        setBot(bot);
        performNextStep(searchTerm, update);
    }
    @Override
    public void performNextStep(String arg, Update update) {
        getBot().setBusy(true);
        Set<String> commandsWithLaterExecution = Set.of("Hole Bilder, Dokumente");
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
        action = "getpics";
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
        getBot().process = null;
    }

    @Override
    public String getProcessName() {
        return "Get-Pics " + searchTerm;
    }
}
