package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.ExecutorUtil;
import com.Utils.IOUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
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

            final Integer[] count = {0};

                listOfDocs.forEach((document1) -> {
                    ObjectHub.getInstance().getExecutorService().submit(new Runnable() {
                        @Override
                        public void run() {
                            Set<String> photoEndings = Set.of("png", "PNG", "jpg", "JPG", "jpeg", "JPEG");
                            String fileExtension = document1.getOriginalFileName().substring(document1.getOriginalFileName().indexOf(".")).replace(".", "");
                            InputMedia media = photoEndings.contains(fileExtension) ? new InputMediaPhoto() : new InputMediaDocument();
                            media.setMedia(document1.getOriginFile(), document1.getOriginalFileName());
                            inputMediaList.add(media);
                            synchronized (count[0]){
                                count[0]++;
                            }
                        }
                    });
                });

        ExecutorUtil.blockUntilLocalCountReached(count[0], listOfDocs.size());
                BotUtil.sendMediaMsg(getBot(), update, true, inputMediaList);
                getBot().setBusy(false);

                BotUtil.sendMsg("Fertig: " + listOfDocs.size() + " Bilder geholt.", getBot(), update, null, true, false);
        setDeleteLater(true);
    }

    @Override
    public String getProcessName() {
        return "Get-Pics " + searchTerm;
    }
}
