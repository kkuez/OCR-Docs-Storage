package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.Document;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.util.*;

public class GetPicsProcess extends Process {

    String searchTerm;

    InputType type = null;

    private static Set<String> commands = Set.of(
            "Dokumente suchen");

    public GetPicsProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        User user = bot.getNonBotUserFromUpdate(update);
        String[] commandValue = deserializeInput(update, bot);
        switch (commandValue[0]) {
            case "Dokumente suchen":
                Message message = bot.sendMsg("Dein Suchbegriff:", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                type = InputType.getPics;
                user.setBusy(false);
                getSentMessages().add(message);
                break;
            case "abort":
                bot.abortProcess(update);
                break;
            default:
                switch (type) {
                    case getPics:
                        List<Document> listOfDocs;
                        searchTerm = commandValue[1];
                        listOfDocs = getFacade().getDocuments(searchTerm);

                        if (listOfDocs.size() == 0) {
                            bot.sendMsg("Keine Dokumente gefunden für den Begriff.", update, null, false, false);
                            reset(bot, user);
                            break;
                        }
                        List<InputMedia> inputMediaList = new ArrayList<>();
                        for (Document document1 : listOfDocs) {
                            Set<String> photoEndings = Set.of("png", "PNG", "jpg", "JPG", "jpeg", "JPEG");
                            String fileExtension = document1.getOriginalFileName().substring(document1.getOriginalFileName().indexOf(".")).replace(".", "");
                            InputMedia media = photoEndings.contains(fileExtension) ? new InputMediaPhoto() : new InputMediaDocument();
                            media.setMedia(document1.getOriginFile(), document1.getOriginalFileName());
                            //Filter for Documents in mediaList.
                            if (media instanceof InputMediaDocument) {
                                //If inputmedia is a document instead of a picture, send it and remove it from the inputmedialist.
                                bot.sendDocument(update, true, (InputMediaDocument) media);
                            } else {
                                inputMediaList.add(media);
                            }
                        }

                        List<Message> messages = bot.sendMediaMsg(update, true, inputMediaList);
                        user.setBusy(false);

                        if (messages.size() > 0) {
                            bot.sendMsg("Fertig: " + listOfDocs.size() + " Bilder geholt.", update, null, true, false);
                        }
                        reset(bot, user);
                        break;
                }

        }
    }

    @Override
    public String getProcessName() {
        return "Get-Pics für Suchterm " + searchTerm;
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        if (type == InputType.getPics) {
            return "getPics";
        }
        return "";
    }

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }

    enum InputType {
        getPics
    }
}
