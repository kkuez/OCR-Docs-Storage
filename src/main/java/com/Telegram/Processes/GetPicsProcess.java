package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.*;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.util.*;

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
        Message message = getBot().sendMsg("Wonach soll gesucht werden?", update, KeyboardFactory.KeyBoardType.Abort, false, true);
        action = "getpics";
        getBot().setBusy(false);
        getSentMessages().add(message);
    }

    private void processInOneStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
                List<Document> listOfDocs;
                if(action != null){
                    searchTerm = item;
                    listOfDocs = DBUtil.getDocumentsForSearchTerm(item);
                }else{
                    String input = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : update.getMessage().getText();
                    searchTerm = input.substring(input.indexOf(" ") + 1);
                    listOfDocs = DBUtil.getDocumentsForSearchTerm(searchTerm);
                }
                List<InputMedia> inputMediaList = new ArrayList<>();

                for(Document document1 : listOfDocs){
                    Set<String> photoEndings = Set.of("png", "PNG", "jpg", "JPG", "jpeg", "JPEG");
                    String fileExtension = document1.getOriginalFileName().substring(document1.getOriginalFileName().indexOf(".")).replace(".", "");
                    InputMedia media = photoEndings.contains(fileExtension) ? new InputMediaPhoto() : new InputMediaDocument();
                    media.setMedia(document1.getOriginFile(), document1.getOriginalFileName());
                    inputMediaList.add(media);
                }

                List<Message> messages = getBot().sendMediaMsg(update, true, inputMediaList);
                getBot().setBusy(false);

                if(messages.size() > 0){
                    getBot().sendMsg("Fertig: " + listOfDocs.size() + " Bilder geholt.", update, null, true, false);
                }
        close();
    }

    @Override
    public String getProcessName() {
        return "Get-Pics " + searchTerm;
    }
}
