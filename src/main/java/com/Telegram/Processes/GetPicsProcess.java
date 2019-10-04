package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.IOUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.util.ArrayList;
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
        BotUtil.sendMsg("Wonach soll gesucht werden?", getBot(), update, null, false, false);
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
                    String input = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : update.getMessage().getText();
                    searchTerm = input.substring(input.indexOf(" ") + 1);
                    listOfDocs = DBUtil.getDocumentsForSearchTerm(searchTerm);
                }
                List<InputMedia> inputMediaPhotoList = new ArrayList<>();
                listOfDocs.forEach(document1 -> {
                    InputMediaPhoto photo = new InputMediaPhoto();
                    photo.setMedia(document1.getOriginFile(), document1.getOriginalFileName());
                    inputMediaPhotoList.add(photo);
                });

                BotUtil.sendMediaMsg(getBot(), update, true, inputMediaPhotoList);
                getBot().setBusy(false);

                BotUtil.sendMsg("Fertig: " + listOfDocs.size() + " Bilder geholt.", getBot(), update, null, true, false);
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
