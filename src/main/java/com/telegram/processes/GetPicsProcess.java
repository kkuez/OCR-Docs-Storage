package com.telegram.processes;

import com.controller.reporter.ProgressReporter;
import com.objectTemplates.Document;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.telegram.KeyboardFactory;
import com.utils.*;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.util.*;

public class GetPicsProcess extends Process {

    String searchTerm;

    Step currentStep = null;

    public GetPicsProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        performNextStep(searchTerm, update, allowedUsersMap);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        String[] commandValue = deserializeInput(update);
        User user = getBot().getNonBotUserFromUpdate(update);
        switch (commandValue[0]){
            case "abort":
                getBot().abortProcess(update);
                break;
            case "getPics":
                List<Document> listOfDocs;
                    searchTerm = commandValue[1];
                    listOfDocs = DBUtil.getDocumentsForSearchTerm(searchTerm);

                if(listOfDocs.size() == 0){
                    getBot().sendMsg("Keine Dokumente gefunden für den Begriff.",  update, null, false, false);
                    close();
                    break;
                }
                List<InputMedia> inputMediaList = new ArrayList<>();
                for(Document document1 : listOfDocs){
                    Set<String> photoEndings = Set.of("png", "PNG", "jpg", "JPG", "jpeg", "JPEG");
                    String fileExtension = document1.getOriginalFileName().substring(document1.getOriginalFileName().indexOf(".")).replace(".", "");
                    InputMedia media = photoEndings.contains(fileExtension) ? new InputMediaPhoto() : new InputMediaDocument();
                    media.setMedia(document1.getOriginFile(), document1.getOriginalFileName());
                    //Filter for Documents in mediaList.
                    if(media instanceof InputMediaDocument){
                        //If inputmedia is a document instead of a picture, send it and remove it from the inputmedialist.
                        getBot().sendDocument(update, true, (InputMediaDocument) media);
                    }else{
                        inputMediaList.add(media);
                    }
                }

                List<Message> messages = getBot().sendMediaMsg(update, true, inputMediaList);
                user.setBusy(false);

                if(messages.size() > 0){
                    getBot().sendMsg("Fertig: " + listOfDocs.size() + " Bilder geholt.", update, null, true, false);
                }
                close();
                break;
                default:
                    Message message = getBot().sendMsg("Wonach soll gesucht werden?", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                    currentStep = Step.getPics;
                    user.setBusy(false);
                    getSentMessages().add(message);
                    break;
        }
    }

    @Override
    public String getProcessName() {
        return "Get-Pics für Suchterm " + searchTerm;
    }

    @Override
    public String getCommandIfPossible(Update update) {
        if(currentStep ==  Step.getPics){
            return "getPics";
        }
        return "";
    }

    enum Step{
        getPics
    }
}
