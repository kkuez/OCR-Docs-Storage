package com.Telegram;

import com.Controller.Reporter.ProgressReporter;
import com.Controller.Reporter.Reporter;
import com.Misc.TaskHandling.Strategies.NextPerformanceStrategy;
import com.Misc.TaskHandling.UpdateTask;
import com.ObjectTemplates.User;
import com.Telegram.Processes.*;
import com.Telegram.Processes.Process;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import com.Utils.TessUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private Boolean isBusy;

    private List<String> shoppingList;

    private Reporter progressReporter;

    public Bot() {
        Map<Integer, User> allowedUsersMap = ObjectHub.getInstance().getAllowedUsersMap();
        shoppingList = DBUtil.getShoppingListFromDB();
        setBusy(false);
        progressReporter = new ProgressReporter() {
            @Override
            public void setTotalSteps(int steps, Update updateOrNull) {
                progressManager.setTotalSteps(steps);
                BotUtil.sendMsg("Start process " +allowedUsersMap.get(updateOrNull.getMessage().getFrom().getId()).getProcess().getProcessName(), Bot.this, updateOrNull, null,false, false);
            }

            @Override
            public void addStep(Update updateOrNull) {
                progressManager.addStep();
                BotUtil.sendMsg( progressManager.getCurrentProgress() + "%", Bot.this, updateOrNull, null,false, false);
            }

            @Override
            public void setStep(int step, Update updateOrNull) {
                progressManager.setCurrentStep(step);
                BotUtil.sendMsg(progressManager.getCurrentProgress() + "%", Bot.this, updateOrNull, null,false, false);
            }
        };
    }

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        Map<Integer, User> allowedUsersMap = ObjectHub.getInstance().getAllowedUsersMap();
        printUpdateData(update);
        int currentUserID;
        String userName;
        String textGivenByUser;
        if(update.hasCallbackQuery()){
            currentUserID = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            textGivenByUser = update.getCallbackQuery().getData();
        }else{
            textGivenByUser = update.getMessage().getText();
            currentUserID = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();
        }

        Process process = null;
        if(allowedUsersMap.get(currentUserID) == null){
           allowedUsersMap.put(currentUserID, new User(currentUserID, userName));
            BotUtil.sendMsg("Hallo " + userName + ", ich hab dich noch nicht im System gefunden, bitte gib das PW für NussBot ein:", this, update, null, true, false);
            allowedUsersMap.get(currentUserID).setProcess(new NewUserRegProcess(this, (ProgressReporter) progressReporter));
            return;
        }

        if(allowedUsersMap.keySet().contains(currentUserID)){
            try {
                processUpdateReceveived(update, allowedUsersMap);
            }catch (Exception e){
                LogUtil.logError(null, e);
                LogUtil.log("Update added to perform later...");
                ObjectHub.getInstance().getTaskshub().getTasksToDo().add(new UpdateTask(update, this, new NextPerformanceStrategy()));
            }
        }else{
            if(process != null && process.getClass().equals(NewUserRegProcess.class)){
                process.performNextStep(textGivenByUser, update, allowedUsersMap);
            }else{

            }
        }
    }
    public void processUpdateReceveived(Update update, Map<Integer, User> allowedUsersMap) throws Exception{
        int currentUserID;
        String userName;
        String textGivenByUser;
        if(update.hasCallbackQuery()){
            currentUserID = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            textGivenByUser = update.getCallbackQuery().getData();
        }else{
            textGivenByUser = update.getMessage().getText();
            currentUserID = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();
        }
        Process process = allowedUsersMap.get(currentUserID).getProcess();

        try {
            if (textGivenByUser != null) {
                String input = textGivenByUser;
                if (process == null) {
                   allowedUsersMap.get(currentUserID).setProcess(fetchCommandOrNull(update, allowedUsersMap));
                   allowedUsersMap.get(currentUserID).deleteProcessEventually(this, update);
                } else {
                    if (getBusy()) {
                        BotUtil.sendMsg("Bin am arbeiten...", this, update,  null, true, false);
                    } else {
                        if (input.startsWith("Japp") || input.startsWith("confirm")) {
                            process.performNextStep("Japp", update, allowedUsersMap);
                        } else {
                            if (input.startsWith("Nee") || input.startsWith("deny")) {
                                process.performNextStep("Nee", update, allowedUsersMap);
                            } else {
                                process.performNextStep(input, update, allowedUsersMap);
                                allowedUsersMap.get(currentUserID).deleteProcessEventually(this, update);
                            }
                        }
                    }
                }
            }
            if (update.getMessage().hasPhoto()) {
                BotUtil.sendMsg("Verarbeite Bild...", this, update,  null, true, false);
                processPhoto(update, allowedUsersMap);
            }
        }catch (Exception e){
            LogUtil.logError(null, e);
            throw new RuntimeException();
        }
    }


    private void printUpdateData(Update update){
        StringBuilder printBuilder;
        if(update.getMessage() == null){
            printBuilder = new StringBuilder(LocalDateTime.now().toString() + ":    Update from " + update.getCallbackQuery().getFrom().getFirstName());
            printBuilder.append(update.getCallbackQuery().getData());
        }else{
            printBuilder = new StringBuilder(LocalDateTime.now().toString() + ":    Update from " + update.getMessage().getFrom().getFirstName());
            String append = update.getMessage().hasPhoto() ? ", new Picture" : "";
            printBuilder.append(append);
            append = update.getMessage().hasText() ? ", cmd: " +update.getMessage().getText() : "";
            printBuilder.append(append);
        }
        LogUtil.log(printBuilder.toString());
    }

    private void processPhoto(Update update, Map<Integer, User> allowedUsersMap){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process process =allowedUsersMap.get(update.getMessage().getFrom().getId()).getProcess();
                setBusy(true);
                File largestPhoto = null;
                List<PhotoSize> photoList = update.getMessage().getPhoto();
                photoList.sort(Comparator.comparing(PhotoSize::getFileSize));
                Collections.reverse(photoList);
                String filePath = getFilePath(photoList.get(0));
                largestPhoto = downloadPhotoByFilePath(filePath);

                if(DBUtil.isFilePresent(largestPhoto)){
                    LogUtil.log("File already present: " + largestPhoto.getName());
                    return;
                }

                File targetFile = new File(ObjectHub.getInstance().getArchiver().getDocumentFolder(), LocalDateTime.now().toString().replace(".", "-").replace(":", "_") + filePath.replace("/", ""));
                try {
                    FileUtils.copyFile(largestPhoto, targetFile);
                } catch (IOException e) {
                    LogUtil.logError(largestPhoto.getAbsolutePath(), e);
                }
                Boolean forceBon = update.getMessage().getCaption() != null && update.getMessage().getCaption().toLowerCase().contains("eatbon");

                Set<String> tags = null;
                if(update.getMessage().getCaption() != null && update.getMessage().getCaption().toLowerCase().startsWith("tag")){
                    tags = parseTags(update.getMessage().getCaption().replace("tag ", ""));
                };

                Document document = TessUtil.processFile(targetFile, update.getMessage().getFrom().getId(), tags);
                try {

                    if((TessUtil.checkIfBon(document.getContent()) || forceBon) && this != null){
                        float sum = TessUtil.getLastNumber(document.getContent());
                        Bon bon = new Bon(document.getContent(), targetFile, sum, document.getId());
                       allowedUsersMap.get(update.getMessage().getFrom().getId()).setProcess(new BonProcess(bon, Bot.this, document, (ProgressReporter) progressReporter, allowedUsersMap));
                    }
                } catch (Exception e) {
                    LogUtil.logError(null, e);
                }

                if (process != null && process.getClass().equals(BonProcess.class)) {
                    sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath(), "Das ist ein Bon oder?", KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Boolean, true, true));
                }
                BotUtil.sendMsg("Fertig.", Bot.this, update,  null, true, false);
                LogUtil.log(update.getMessage().getText());
                setBusy(false);
            }
        });
        thread.start();
    }

    private Set<String> parseTags(String input){
        input = input.toLowerCase().replace("tag ", "");
        Set<String> tags = new HashSet<>();
        while (input.contains(",")){
            String tag = input.substring(0, input.indexOf(","));
            tags.add(tag);
            input = input.replace(tag, "").replaceFirst(",", "");
        }

        tags.add(input);
        return tags;
    }

    public void sendDocumentFromURL(Update update, String imagePath, String caption, ReplyKeyboardMarkup possibleKeyBoardOrNull){
        SendDocument sendDocument = null;
        try {
            sendDocument = new SendDocument().setDocument(caption, new FileInputStream(new File(imagePath)));
            sendDocument.setCaption(caption);
            if(possibleKeyBoardOrNull != null){
                sendDocument.setReplyMarkup(possibleKeyBoardOrNull);
            }
        } catch (FileNotFoundException e) {
            LogUtil.logError(imagePath, e);
        }
        sendDocument.setChatId(update.getMessage().getChatId());
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
            BotUtil.sendMsg("Fehler, Aktion abgebrochen.",this, update,  null, true, false);
            setBusy(false);
        }
    }

    public void sendPhotoFromURL(Update update, String imagePath, String caption, ReplyKeyboard possibleKeyBoardOrNull){
        SendPhoto sendPhoto = null;
        try {
            sendPhoto = new SendPhoto().setPhoto("SomeText", new FileInputStream(new File(imagePath)));
            sendPhoto.setCaption(caption);
            if(possibleKeyBoardOrNull != null){
                sendPhoto.setReplyMarkup(possibleKeyBoardOrNull);
            }
        } catch (FileNotFoundException e) {
            LogUtil.logError(imagePath, e);
            return;
        }
        sendPhoto.setChatId(update.getMessage().getChatId());
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            setBusy(false);
            BotUtil.sendMsg("Fehler, Aktion abgebrochen.",this, update,  null, true, false);
            LogUtil.logError(null, e);
        }
    }

    public java.io.File downloadPhotoByFilePath(String filePath) {
        try {
            // Download the file calling AbsSender::downloadFile method
            return downloadFile(filePath);
        } catch (TelegramApiException e) {
            LogUtil.logError(filePath, e);
        }
        return null;
    }

    public String getFilePath(PhotoSize photo) {
        Objects.requireNonNull(photo);

        if (photo.hasFilePath()) { // If the file_path is already present, we are done!
            return photo.getFilePath();
        } else { // If not, let find it
            // We create a GetFile method and set the file_id from the photo
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(photo.getFileId());
            try {
                // We execute the method using AbsSender::execute method.
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);
                // We now have the file_path
                return file.getFilePath();
            } catch (TelegramApiException e) {
                LogUtil.logError(null, e);
            }
        }
        return null; // Just in case
    }

    private Process fetchCommandOrNull(Update update, Map<Integer, User> allowedUsersMap){
        int currentUserID;
        String userName;
        String textGivenByUser;
        if(update.hasCallbackQuery()){
            currentUserID = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            textGivenByUser = update.getCallbackQuery().getData();
        }else{
            textGivenByUser = update.getMessage().getText();
            currentUserID = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();
        }
        if(textGivenByUser != null) {
            String input = textGivenByUser;

            String cmd = input.contains(" ") ? input.substring(0, input.indexOf(" ")).toLowerCase().replace("/", "") : input.toLowerCase().replace("/", "");

            if (cmd.startsWith("start")) {
                return new StartProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
            } else {
                if (cmd.startsWith("search") || input.equals("Anzahl Dokumente")) {
                    return new SearchProcess(this, update,(ProgressReporter) progressReporter, allowedUsersMap);
                } else {
                    if (cmd.startsWith("getpics") || input.equals("Hole Bilder, Dokumente")) {
                        return new GetPicsProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
                    } else {
                        if (cmd.startsWith("getsum") || input.equals("Summe von Bons")) {
                            return new SumProcess(this, (ProgressReporter) progressReporter, update, allowedUsersMap);
                        } else {
                            if (cmd.startsWith("getbons") || input.equals("Hole Bons")) {
                                return new GetBonsProcess(this, (ProgressReporter) progressReporter, update, allowedUsersMap);
                            } else {
                                if (cmd.startsWith("removelast") || input.equals("Letztes Bild Löschen")) {
                                    return new RemoveLastProcess(this, (ProgressReporter) progressReporter, allowedUsersMap);
                                }else{
                                    if(cmd.startsWith("einkaufslisten-optionen")) {
                                        BotUtil.sendMsg("Was willst du tun?",this, update,  KeyboardFactory.KeyBoardType.ShoppingList, true, false);
                                    } else {
                                        if (cmd.startsWith("add") || (input.equals("Hinzufügen") || cmd.startsWith("removeitem") || input.equals("Item Löschen") || cmd.startsWith("getlist") || input.equals("Einkaufsliste anzeigen") || cmd.startsWith("removeall") || input.equals("Ganze Liste Löschen"))) {
                                            return new ShoppingListProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method returns the bot's name, which was specified during registration.
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return "NussBot";
    }

    /**
     * This method returns the bot's token for communicating with the Telegram server
     * @return the bot's token
     */
    @Override
    public String getBotToken() {
        return ObjectHub.getInstance().getProperties().getProperty("tgBotToken");
    }
    public static void enterCommands(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.log("Command 1 getupdate");
                while(true){
                    Scanner scanner = new Scanner(System.in);
                    switch (scanner.next()){
                        case "1":

                            break;
                    }
                }
            }
        });
    }



    //GETTER SETTER


    public List<String> getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(List<String> shoppingList) {
        this.shoppingList = shoppingList;
    }

    public Boolean getBusy() {
        return isBusy;
    }

    public void setBusy(Boolean busy) {
        isBusy = busy;
    }
}