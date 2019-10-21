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
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import com.Utils.TessUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

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
                sendMsg("Start process " +allowedUsersMap.get(updateOrNull.getMessage().getFrom().getId()).getProcess().getProcessName(),  updateOrNull, null,false, false);
            }

            @Override
            public void addStep(Update updateOrNull) {
                progressManager.addStep();
                sendMsg( progressManager.getCurrentProgress() + "%", updateOrNull, null,false, false);
            }

            @Override
            public void setStep(int step, Update updateOrNull) {
                progressManager.setCurrentStep(step);
                sendMsg(progressManager.getCurrentProgress() + "%", updateOrNull, null,false, false);
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
            sendMsg("Hallo " + userName + ", ich hab dich noch nicht im System gefunden, bitte gib das PW für NussBot ein:", update, null, true, false);
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
                            sendMsg("Bitte lad jetzt den Bon hoch.", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                        }
                    } else {
                        if (getBusy()) {
                            sendMsg("Bin am arbeiten...", update, KeyboardFactory.KeyBoardType.Abort, true, true);
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
                        sendMsg("Bin am arbeiten...", update, KeyboardFactory.KeyBoardType.Abort, true, true);
                    } else {
                        if(process != null && process.isAwaitsInput()){
                            process.setAwaitsInput(false);
                            process.performNextStep("comingPic", update, allowedUsersMap);
                            allowedUsersMap.get(currentUserID).deleteProcessEventually(this, update);
                        }else {
                            sendMsg("Verarbeite Bild...", update, null, true, false);
                            processPhoto(update, allowedUsersMap);
                        }
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
                    sendMsg("Bild schon vorhanden.", update, null, true, false);
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
                    sendMsg("Fertig.", update,  null, true, false);
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
            sendMsg("Fehler, Aktion abgebrochen.",update,  null, true, false);
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
            sendMsg("Fehler, Aktion abgebrochen.",update,  null, true, false);
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
                    sendMsg("Was willst du tun?",update,  KeyboardFactory.KeyBoardType.Bons, true, false);
                    break;
                case "Einkaufslisten-Optionen":
                    sendMsg("Was willst du tun?", update, KeyboardFactory.KeyBoardType.ShoppingList, true, false);
                    break;
                case "Hinzufügen":
                case "Löschen":
                case "Liste anzeigen":
                case "Liste Löschen":
                    processToReturn = new ShoppingListProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
                    break;
                case "Standardlisten-Optionen":
                    sendMsg("Was willst du tun?", update, KeyboardFactory.KeyBoardType.StandardList, true, false);
                    break;
                case "Standardliste anzeigen":
                case "Item hinzufügen":
                case "Item löschen":
                    processToReturn = new StandardListProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
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
    public  void enterCommands(){
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
            this.setBusy(false);
            allowedUsersMap.get(currentUserID).getProcess().close();
            String processName = allowedUsersMap.get(currentUserID).getProcess().getProcessName();
            LogUtil.log("User " + allowedUsersMap.get(currentUserID).getName() + " aborts " + allowedUsersMap.get(currentUserID).getProcess().getProcessName() + " Process.");
            allowedUsersMap.get(currentUserID).setProcess(null);
            sendMsg(processName + " abgebrochen.", update, null, false, false);
            try {
                sendAnswerCallbackQuery(processName + " abgebrochen.",  false, update.getCallbackQuery());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }else{
            try {
                simpleEditMessage("Abgebrochen", update, KeyboardFactory.KeyBoardType.NoButtons);
                sendAnswerCallbackQuery("Abgebrochen",  false, update.getCallbackQuery());
            } catch (TelegramApiException e) {
LogUtil.logError(e.getMessage(), e);
            }
        }
        }


    public  Message sendKeyboard(String s, Update update, ReplyKeyboard replyKeyboard, boolean isReply){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(getMassageFromUpdate(update).getChatId());
        if(isReply){
            sendMessage.setReplyToMessageId(getMassageFromUpdate(update).getMessageId());
        }
        sendMessage.setReplyMarkup(replyKeyboard);
        sendMessage.setText(s);

        Message messageToReturn = null;
        try {
            messageToReturn =  execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
        return messageToReturn;
    }

    public  Message askBoolean(String question, Update update,boolean isReply){
        Message message = null;
        if(update.hasCallbackQuery()){
            message = simpleEditMessage(question, update, KeyboardFactory.KeyBoardType.Boolean);
        }else{
            message = sendMsg(question, update, KeyboardFactory.KeyBoardType.Boolean, isReply, true);
        }
        return message;
    }
    public  Message askMonth(String question, Update update,  boolean isReply) {
        Message message = null;
        if (update.hasCallbackQuery()) {
            message = simpleEditMessage(question,  update, KeyboardFactory.KeyBoardType.Calendar_Month);
        } else {
            message = sendMsg(question,  update, KeyboardFactory.KeyBoardType.Calendar_Month, isReply, true);
        }
        return message;
    }
    public  void editCaption(String text,  Message message){
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setChatId(message.getChatId() + "");
        editMessageCaption.setMessageId(message.getMessageId());
        editMessageCaption.setCaption(text);
        try {
            execute(editMessageCaption);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public  void editMessage(String text, Bot bot, Message message){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(message.getChatId());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(text);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //Convenience method to have one edit method for everything
    public  Message simpleEditMessage(String text,  Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull){
        Message message = getMassageFromUpdate(update);
        return simpleEditMessage(text,  message, keyBoardTypeOrNull);
    }
    public  Message simpleEditMessage(String text, Message message, KeyboardFactory.KeyBoardType keyBoardTypeOrNull){

        if(!message.hasText()){
            if(message.hasPhoto() && message.getCaption() != null){
                editCaption(text,  message);
            }
        }else{
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(message.getChatId());
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setText(text);
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if(keyBoardTypeOrNull != null && message.hasReplyMarkup()) {
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(message.getChatId());
            editMessageReplyMarkup.setMessageId(message.getMessageId());
            editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) KeyboardFactory.getKeyBoard(keyBoardTypeOrNull, true, false));
            try {
                execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                if(((TelegramApiRequestException) e).getApiResponse().contains("message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message")){
                    LogUtil.log("Message not edited, no need.");
                }else{
                    LogUtil.logError(((TelegramApiRequestException) e).getApiResponse(), e);
                }
            }
        }
        return message;
    }

    public  Message askYear(String question, Update update, boolean isReply){
        Message message = null;
        if (update.hasCallbackQuery()) {
            message = simpleEditMessage(question, update, KeyboardFactory.KeyBoardType.Calendar_Year);
        } else {
            message = sendMsg(question,  update, KeyboardFactory.KeyBoardType.Calendar_Year, isReply, true);
        }
        return message;
    }

    /**
     * Method for creating a message and sending it.
     * @param chatId chat id
     * @param s The String that you want to send as a message.
     *
     */

    /**
     *Documents cannot be send in groups like pictures
     */
    public  Message sendDocument(Update update,  boolean isReply,  InputMediaDocument inputMediaDocument){
        Message message = getMassageFromUpdate(update);
        long chatID = message.getChatId();
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(inputMediaDocument.getMediaFile());
        sendDocument.setChatId(chatID);
        Message messageToReturn = null;
        if(isReply){
            sendDocument.setReplyToMessageId(message.getMessageId());
        }
        try {
            messageToReturn = execute(sendDocument);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
        return messageToReturn;
    }

    public  synchronized List<Message> sendMediaMsg(Update update,  boolean isReply,  List<InputMedia> inputMediaList) {
        if(inputMediaList.size() == 0){
            sendMsg("Keine Dokumente gefunden für den Begriff.",  update, null, false, false);
            return new ArrayList<>();
        }
        Message message = getMassageFromUpdate(update);
        long chatID = message.getChatId();
        List<InputMedia> toBeRemovedList = new ArrayList<>();
        for(InputMedia inputMedia : inputMediaList){
            if(inputMedia instanceof InputMediaDocument){
                sendDocument(update, true, (InputMediaDocument) inputMedia);
                toBeRemovedList.add(inputMedia);
            }
        }
        inputMediaList.removeAll(toBeRemovedList);

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setMedia(inputMediaList);
        sendMediaGroup.setChatId(chatID);
        if(isReply){
            sendMediaGroup.setReplyToMessageId(message.getMessageId());
        }
        List<Message> messageToReturn = null;
        try {
            messageToReturn = execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
            sendMsg("Zuviele Dokumente gefunden für den Begriff... Abgebrochen.",  update, null, false, false);
            abortProcess(update, ObjectHub.getInstance().getAllowedUsersMap(), update.getMessage().getFrom().getId());
        }

        return messageToReturn;
    }
    public  synchronized Message sendMsg(String s, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, boolean isReply, boolean inlineKeyboard) {
        boolean isOneTimeKeyboard = false;
        Message message = getMassageFromUpdate(update);
        long chatID = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID);
        if(isReply){
            sendMessage.setReplyToMessageId(message.getMessageId());
        }
        if(keyBoardTypeOrNull != null){
            sendMessage.setReplyMarkup(KeyboardFactory.getKeyBoard(keyBoardTypeOrNull, inlineKeyboard, isOneTimeKeyboard));
        }
        sendMessage.setText(s);
        Message messageToReturn = null;
        try {
            messageToReturn = execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
        return messageToReturn;
    }

    public  Message getMassageFromUpdate(Update update){
        return update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() : update.getMessage();
    }

    public   void sendAnswerCallbackQuery(String text, boolean alert, CallbackQuery callbackquery) throws TelegramApiException{
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackquery.getId());
        answerCallbackQuery.setShowAlert(alert);
        answerCallbackQuery.setText(text);
        execute(answerCallbackQuery);
    }
    public void deleteSLIDESHOWMESSAGE(Message message){
        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setChatId(message.getChatId());
        editMessageMedia.setMedia(new InputMediaPhoto());
        editMessageMedia.setReplyMarkup((InlineKeyboardMarkup) KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.NoButtons, true, false));
        editMessageMedia.setMessageId(message.getMessageId());
    try {
            execute(editMessageMedia);
        } catch (TelegramApiException e) {
            LogUtil.logError("", e);
        }
    }

    public Message sendOrEditSLIDESHOWMESSAGE(String text, Item item,  Update update){
        Message messageToReturn = getMassageFromUpdate(update);
        try(FileInputStream photoPath =  new FileInputStream(item.getPicturePath())){
        if(update.hasCallbackQuery()){
            //edit Fall
            EditMessageMedia editMessageMedia = new EditMessageMedia();
            editMessageMedia.setChatId(messageToReturn.getChatId());
            InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
            inputMediaPhoto.setMedia(item.getPicturePath(), item.getName());
            inputMediaPhoto.setCaption(item.getName());
            editMessageMedia.setMedia(inputMediaPhoto);
            editMessageMedia.getMedia().setCaption(item.getName());
            editMessageMedia.setMessageId(messageToReturn.getMessageId());
            editMessageMedia.setReplyMarkup(messageToReturn.getReplyMarkup());
            execute(editMessageMedia);
            messageToReturn = update.getCallbackQuery().getMessage();
            sendAnswerCallbackQuery(text, false, update.getCallbackQuery());
        }else{
            //firsche Nachricht
            SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setPhoto(item.getName(), photoPath);
                sendPhoto.setChatId(update.getMessage().getChatId());
                sendPhoto.setReplyMarkup(KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.SlideShow, true, false));
                sendPhoto.setCaption(item.getName());
               messageToReturn = execute(sendPhoto);
        }
        } catch (FileNotFoundException e) {
            LogUtil.logError("", e);
        } catch (IOException e) {
            LogUtil.logError("", e);
        } catch (TelegramApiException e) {
            LogUtil.logError("", e);
        }
        return messageToReturn;
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