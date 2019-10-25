package com.Telegram;

import com.Controller.Reporter.ProgressReporter;
import com.Controller.Reporter.Reporter;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private List<String> shoppingList;

    private Reporter progressReporter;

    Map<Integer, User> allowedUsersMap;

    public Bot(Map<Integer, User> allowedUsersMap) {
        this.allowedUsersMap = allowedUsersMap;
        shoppingList = DBUtil.getShoppingListFromDB();
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
        printUpdateData(update);
        int currentUserID;
        String userName;
        String textGivenByUser;
        if(update.hasCallbackQuery()){
            currentUserID = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            textGivenByUser = update.getCallbackQuery().getData();
            if (textGivenByUser.equals("abort")){
                abortProcess(update);
            }
        }else{
            currentUserID = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();
        }

        //Check if user is in System
        if(allowedUsersMap.get(currentUserID) == null){
            allowedUsersMap.put(currentUserID, new User(currentUserID, userName));
            sendMsg("Hallo " + userName + ", ich hab dich noch nicht im System gefunden, bitte gib das PW für NussBot ein:", update, null, true, false);
            allowedUsersMap.get(currentUserID).setProcess(new NewUserRegProcess(this, (ProgressReporter) progressReporter));
            return;
        }
        try {
            processUpdateReceveived(update);
        }catch (Exception e){
            LogUtil.logError("Couldn't process update.", e);
        }
    }

    public void processUpdateReceveived(Update update) throws Exception{
        int currentUserID;
        String textGivenByUser;
        boolean isBusy = getNonBotUserFromUpdate(update).isBusy();
        if(update.hasCallbackQuery()){
            currentUserID = update.getCallbackQuery().getFrom().getId();
            textGivenByUser = update.getCallbackQuery().getData();
        }else{
            textGivenByUser = update.getMessage().getText();
            currentUserID = update.getMessage().getFrom().getId();
        }
        Process process = allowedUsersMap.get(currentUserID).getProcess();

        try {
            if (isBusy) {
                sendMsg("Bin am arbeiten...", update, KeyboardFactory.KeyBoardType.Abort, true, true);
            } else {
                if (process == null) {
                    //Set process if null
                    if (update.hasMessage() && update.getMessage().hasPhoto()) {
                        sendMsg("Verarbeite Bild...", update, null, true, false);
                        processPhoto(update);
                    }else{
                        allowedUsersMap.get(currentUserID).setProcess(fetchCommandOrNull(update));
                        allowedUsersMap.get(currentUserID).deleteProcessEventually(this, update);
                    }
                } else {
                    if (update.hasMessage() && update.getMessage().hasPhoto()) {
                        sendMsg("Verarbeite Bild...", update, null, true, false);
                        processPhoto(update);
                    }else{
                        process.performNextStep(textGivenByUser, update, allowedUsersMap);
                        allowedUsersMap.get(currentUserID).deleteProcessEventually(this, update);
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

    private void processPhoto(Update update){
        ObjectHub.getInstance().getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                User user = getNonBotUserFromUpdate(update);
                Process process = user.getProcess();
                user.setBusy(true);
                File largestPhoto = null;
                List<PhotoSize> photoList = update.getMessage().getPhoto();
                photoList.sort(Comparator.comparing(PhotoSize::getFileSize));
                Collections.reverse(photoList);
                //Get largest picture
                String filePath = getFilePath(photoList.get(0));
                largestPhoto = downloadPhotoByFilePath(filePath);

                if(DBUtil.isFilePresent(largestPhoto)){
                    //Is File already stored...?
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

                Set<String> tags = null;
                if(update.getMessage().getCaption() != null && update.getMessage().getCaption().toLowerCase().startsWith("tag")){
                    tags = parseTags(update.getMessage().getCaption().replace("tag ", ""));
                };

                Document document = TessUtil.processFile(targetFile, update.getMessage().getFrom().getId(), tags);
                try {
                    if((TessUtil.checkIfBon(document.getContent()) || process instanceof BonProcess)){
                        float sum = TessUtil.getLastNumber(document.getContent());
                        Bon bon = new Bon(document.getContent(), targetFile, sum, document.getId());
                        BonProcess bonProcess = (BonProcess) process;
                        bonProcess.setBon(bon);
                    }
                } catch (Exception e) {
                    LogUtil.logError(null, e);
                }

                if (process != null && process.getClass().equals(BonProcess.class)) {
                    sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath(), "Das ist ein Bon oder?", KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Boolean, true, true, ""));
                }else{
                    sendMsg("Fertig.", update,  null, true, false);
                }
                LogUtil.log("Processed " + document.getOriginalFileName());
                user.setBusy(false);
            }
        });
    }

    private Set<String> parseTags(String input){
        //Tags have to be input by Caption
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

    public void sendPhotoFromURL(Update update, String imagePath, String caption, ReplyKeyboard possibleKeyBoardOrNull){
        SendPhoto sendPhoto = null;
        User user = getNonBotUserFromUpdate(update);
        try {
            sendPhoto = new SendPhoto().setPhoto("SomeText", new FileInputStream(new File(imagePath)));
            sendPhoto.setCaption(caption);
            if(possibleKeyBoardOrNull != null){
                sendPhoto.setReplyMarkup(possibleKeyBoardOrNull);
                if(!(possibleKeyBoardOrNull instanceof InlineKeyboardMarkup)){
                    //In case of an Inlinekeyboard it will not be stored as keyboardcontext
                    user.setKeyboardContext(possibleKeyBoardOrNull);
                }
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
            user.setBusy(false);
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

    private Process fetchCommandOrNull(Update update){
        String textGivenByUser = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : update.getMessage().getText();

        Process processToReturn = null;
        if(textGivenByUser != null) {
            switch (textGivenByUser){
                case "Bon eingeben":
                    sendMsg("Bitte lad jetzt den Bon hoch.", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                    processToReturn = new BonProcess(this, (ProgressReporter) progressReporter);
                    break;
                case "Standardliste: Optionen":
                    processToReturn = new StandardListProcess((ProgressReporter) progressReporter, this, update, allowedUsersMap);
                    sendKeyboard("Was willst du tun?", update, KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.StandardList, false, false, ""), false);
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
                case "Kalender-Optionen":
                    sendMsg("Was willst du tun?",update,  KeyboardFactory.KeyBoardType.Calendar, true, false);
                    break;
                case "Termine anzeige":
                case "Termin hinzufügen":
                case "Termin löschen":
                    processToReturn = new CalenderProcess((ProgressReporter) progressReporter, this, update, allowedUsersMap);
                    break;
                case "Einkaufslisten-Optionen":
                    sendMsg("Was willst du tun?", update, KeyboardFactory.KeyBoardType.ShoppingList, true, false);
                    break;
                case "Hinzufügen":
                case "Löschen":
                case "Liste anzeigen":
                case "Liste Löschen":
                    if(!update.hasCallbackQuery()) {
                        boolean isStadardListConText = allowedUsersMap.get(update.getMessage().getFrom().getId()).getKeyboardContext().equals(KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.StandardList, false, false, ""));
                        if (isStadardListConText) {
                            processToReturn = new StandardListProcess((ProgressReporter) progressReporter, this, update, allowedUsersMap);
                        } else {
                            processToReturn = new ShoppingListProcess(this, update, (ProgressReporter) progressReporter, allowedUsersMap);
                        }
                    }
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
    public User getNonBotUserFromUpdate(Update update){
        int userId = update.hasCallbackQuery() ? update.getCallbackQuery().getFrom().getId() : update.getMessage().getFrom().getId();
        return allowedUsersMap.get(userId);
    }

    public void abortProcess(Update update){
        User user = getNonBotUserFromUpdate(update);
        if(user.getProcess() != null) {
            user.setBusy(false);
            String processName = user.getProcess().getProcessName();
            LogUtil.log("User " + user.getName() + " aborts " + user.getProcess().getProcessName() + " Process.");
            user.getProcess().close();
            try {
                sendMsg(processName + " abgebrochen.", update, null, false, false);
                if(update.hasCallbackQuery()) {
                    sendAnswerCallbackQuery(processName + " abgebrochen.", false, update.getCallbackQuery());
                }
            } catch (TelegramApiException e) {
                LogUtil.logError("Abort done, messaging about abort failed.", e);
            }
        }else{
            try {
                simpleEditMessage("Abgebrochen", update, KeyboardFactory.KeyBoardType.NoButtons);
                if(update.hasCallbackQuery()){
                    sendAnswerCallbackQuery("Abgebrochen",  false, update.getCallbackQuery());
                }
            } catch (TelegramApiException e) {
                LogUtil.logError("Abort done, messaging about abort failed.", e);
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
        if(!(replyKeyboard instanceof InlineKeyboardMarkup)){
            allowedUsersMap.get(getMassageFromUpdate(update).getFrom().getId()).setKeyboardContext(replyKeyboard);
        }
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
    public  Message askMonth(String question, Update update,  boolean isReply, String callbackPrefix) {
        Message message = null;
        if (update.hasCallbackQuery()) {
            message = simpleEditMessage(question,  update, KeyboardFactory.KeyBoardType.Calendar_Month, callbackPrefix);
        } else {
            message = sendMsg(question,  update, KeyboardFactory.KeyBoardType.Calendar_Month, callbackPrefix, isReply, true);
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
            LogUtil.logError("Couldn't edit caption.", e);
        }
    }

    //Convenience method to have one edit method for everything
    public  Message simpleEditMessage(String text,  Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, String callBackPrefix){
        Message message = getMassageFromUpdate(update);
        return simpleEditMessage(text,  message, keyBoardTypeOrNull, callBackPrefix);
    }
    public  Message simpleEditMessage(String text,  Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull){
        Message message = getMassageFromUpdate(update);
        return simpleEditMessage(text,  message, keyBoardTypeOrNull, "");
    }
    public  Message simpleEditMessage(String text, Message message, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, String callbackPrefix){
        List<List<InlineKeyboardButton>> inputKeyboard = KeyboardFactory.createInlineKeyboard(keyBoardTypeOrNull, callbackPrefix);
        return simpleEditMessage(text, message, inputKeyboard, callbackPrefix);
    }
    public  Message simpleEditMessage(String text, Message message, List<List<InlineKeyboardButton>> inputKeyboard, String callbackPrefix){

        if(!message.hasText()){
            if(message.hasPhoto() && message.getCaption() != null){
                editCaption(text,  message);
            }
        }else{
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(message.getChatId());
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setText(text);

            editMessageText.setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(inputKeyboard));
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                LogUtil.logError("Couldn't edit message text.", e);
            }
        }
        if(inputKeyboard != null && message.hasReplyMarkup()) {
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(message.getChatId());
            editMessageReplyMarkup.setMessageId(message.getMessageId());
            editMessageReplyMarkup.setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(inputKeyboard));
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

    public Message askYear(String question, Update update, boolean isReply, String callbackPrefix){
        Message message = null;
        if (update.hasCallbackQuery()) {
            message = simpleEditMessage(question, update, KeyboardFactory.KeyBoardType.Calendar_Year, callbackPrefix);
        } else {
            message = sendMsg(question,  update, KeyboardFactory.KeyBoardType.Calendar_Year, callbackPrefix, isReply, true);
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
            LogUtil.logError("Failed to send Document message.", e);
        }
        return messageToReturn;
    }

    public  synchronized List<Message> sendMediaMsg(Update update,  boolean isReply,  List<InputMedia> inputMediaList) {
        Message message = getMassageFromUpdate(update);
        long chatID = message.getChatId();

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setMedia(inputMediaList);
        sendMediaGroup.setChatId(chatID);
        if(isReply){
            sendMediaGroup.setReplyToMessageId(message.getMessageId());
        }
        List<Message> messageToReturn = null;
        try {
            if(inputMediaList.size() == 0){
                throw new TelegramApiException("No media in List found.");
            }
            messageToReturn = execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
            sendMsg("Failed to send mediaGroup.",  update, null, false, false);
            abortProcess(update);
        }
        return messageToReturn;
    }
    public  synchronized Message sendMsg(String s, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, boolean isReply, boolean inlineKeyboard) {
        return sendMsg(s, update, keyBoardTypeOrNull, "", isReply, inlineKeyboard);
    }
    public  synchronized Message sendMsg(String s, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, String callbackValuePrefix,  boolean isReply, boolean inlineKeyboard) {
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
            ReplyKeyboard replyKeyboard = KeyboardFactory.getKeyBoard(keyBoardTypeOrNull, inlineKeyboard, isOneTimeKeyboard, callbackValuePrefix);
            sendMessage.setReplyMarkup(replyKeyboard);
            if(!inlineKeyboard){
                //If no InlineKeyboard set the keyboardcontext to the incoming keyboard. Therefore making sure the list processes get the certain keyboards as context.
                allowedUsersMap.get(update.getMessage().getFrom().getId()).setKeyboardContext(replyKeyboard);
            }
        }
        sendMessage.setText(s);
        Message messageToReturn = null;
        try {
            messageToReturn = execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError("Failed to send message.", e);
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
    //GETTER SETTER


    public List<String> getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(List<String> shoppingList) {
        this.shoppingList = shoppingList;
    }
}