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
import org.telegram.telegrambots.meta.api.objects.Message;
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
            if (textGivenByUser.equals("abort")){
                abortProcess(update, allowedUsersMap, currentUserID);
            }
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
                        if (input.startsWith("Bon eingeben")) {
                            allowedUsersMap.get(currentUserID).setAboutToUploadFile(true);
                            BotUtil.sendMsg("Bitte lad jetzt den Bon hoch.", this, update, KeyboardFactory.KeyBoardType.Abort, false, true);
                        }
                    } else {
                        if (getBusy()) {
                            BotUtil.sendMsg("Bin am arbeiten...", this, update, KeyboardFactory.KeyBoardType.Abort, true, true);
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
                if (update.hasMessage() && update.getMessage().hasPhoto()) {
                    if (getBusy()) {
                        BotUtil.sendMsg("Bin am arbeiten...", this, update, KeyboardFactory.KeyBoardType.Abort, true, true);
                    } else {
                        BotUtil.sendMsg("Verarbeite Bild...", this, update, null, true, false);
                        processPhoto(update, allowedUsersMap);

                    }
                    }
            } catch (Exception e) {
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
        ObjectHub.getInstance().getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                Process process = allowedUsersMap.get(update.getMessage().getFrom().getId()).getProcess();
                setBusy(true);
                File largestPhoto = null;
                List<PhotoSize> photoList = update.getMessage().getPhoto();
                photoList.sort(Comparator.comparing(PhotoSize::getFileSize));
                Collections.reverse(photoList);
                String filePath = getFilePath(photoList.get(0));
                largestPhoto = downloadPhotoByFilePath(filePath);

                if(DBUtil.isFilePresent(largestPhoto)){
                    LogUtil.log("File already present: " + largestPhoto.getName());
                    BotUtil.sendMsg("Bild schon vorhanden.", Bot.this, update, null, true, false);
                    return;
                }

                File targetFile = new File(ObjectHub.getInstance().getArchiver().getDocumentFolder(), LocalDateTime.now().toString().replace(".", "-").replace(":", "_") + filePath.replace("/", ""));
                try {
                    FileUtils.copyFile(largestPhoto, targetFile);
                } catch (IOException e) {
                    LogUtil.logError(largestPhoto.getAbsolutePath(), e);
                }
                Boolean forceBon = update.getMessage().getCaption() != null && update.getMessage().getCaption().toLowerCase().contains("eatbon") || allowedUsersMap.get(update.getMessage().getFrom().getId()).isAboutToUploadFile();

                Set<String> tags = null;
                if(update.getMessage().getCaption() != null && update.getMessage().getCaption().toLowerCase().startsWith("tag")){
                    tags = parseTags(update.getMessage().getCaption().replace("tag ", ""));
                };

                Document document = TessUtil.processFile(targetFile, update.getMessage().getFrom().getId(), tags);
                try {

                    if((TessUtil.checkIfBon(document.getContent()) || forceBon) && this != null){
                        float sum = TessUtil.getLastNumber(document.getContent());
                        Bon bon = new Bon(document.getContent(), targetFile, sum, document.getId());
                        process = new BonProcess(bon, Bot.this, document, (ProgressReporter) progressReporter, allowedUsersMap);
                       allowedUsersMap.get(update.getMessage().getFrom().getId()).setProcess(process);
                        allowedUsersMap.get(update.getMessage().getFrom().getId()).setAboutToUploadFile(false);

                    }
                } catch (Exception e) {
                    LogUtil.logError(null, e);
                }

                if (process != null && process.getClass().equals(BonProcess.class)) {
                    sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath(), "Das ist ein Bon oder?", KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Boolean, true, true));
                }else{
                    BotUtil.sendMsg("Fertig.", Bot.this, update,  null, true, false);
                }
                LogUtil.log("Processed " + document.getOriginalFileName());
                setBusy(false);
            }
        });
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
        long chatID = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getFrom().getId();
        sendPhoto.setChatId(chatID);
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
        String textGivenByUser = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : update.getMessage().getText();

        Process processToReturn = null;
        if(textGivenByUser != null) {
            String input = textGivenByUser;
            switch (input){
                case "Anzahl Dokumente":
                    processToReturn = new SearchProcess(this, update,(ProgressReporter) progressReporter, allowedUsersMap);
                    break;
                case "start":
                case "Start":
                    processToReturn = new StartProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
                    break;
                case "Hole Bilder, Dokumente":
                    processToReturn = new GetPicsProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
                    break;
                case "Summe von Bons":
                    processToReturn = new SumProcess(this, (ProgressReporter) progressReporter, update, allowedUsersMap);
                    break;
                case "Hole Bons":
                    processToReturn =  new GetBonsProcess(this, (ProgressReporter) progressReporter, update, allowedUsersMap);
                    break;
                case "Letztes Bild Löschen":
                    processToReturn = new RemoveLastProcess(this, (ProgressReporter) progressReporter, update, allowedUsersMap);
                    break;
                case "Bon-Optionen":
                    BotUtil.sendMsg("Was willst du tun?",this, update,  KeyboardFactory.KeyBoardType.Bons, true, false);
                    break;
                case "Einkaufslisten-Optionen":
                    BotUtil.sendMsg("Was willst du tun?", this, update, KeyboardFactory.KeyBoardType.ShoppingList, true, false);
                    break;
                case "Hinzufügen":
                case "Löschen":
                case "Liste anzeigen":
                case "Liste Löschen":
                    processToReturn = new ShoppingListProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
                    break;
            }
        }
        return processToReturn;
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

    public void abortProcess(Update update, Map<Integer, User> allowedUsersMap, int currentUserID){
        if(allowedUsersMap.get(currentUserID).getProcess() != null) {
            Bot.this.setBusy(false);
            allowedUsersMap.get(currentUserID).getProcess().close();
            String processName = allowedUsersMap.get(currentUserID).getProcess().getProcessName();
            LogUtil.log("User " + allowedUsersMap.get(currentUserID).getName() + " aborts " + allowedUsersMap.get(currentUserID).getProcess().getProcessName() + " Process.");
            allowedUsersMap.get(currentUserID).setProcess(null);
            BotUtil.sendMsg(processName + " abgebrochen.", Bot.this, update, null, false, false);
            BotUtil.sendAnswerCallbackQuery(processName + " abgebrochen.", this, false, update.getCallbackQuery());
        }else{
            try {
                BotUtil.simpleEditMessage("Abgebrochen", this, update, KeyboardFactory.KeyBoardType.NoButtons);
                BotUtil.sendAnswerCallbackQuery("Abgebrochen", this, false, update.getCallbackQuery());
            } catch (TelegramApiException e) {
LogUtil.logError(e.getMessage(), e);
            }
        }
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