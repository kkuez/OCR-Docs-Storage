package com.bot.telegram;

import com.Main;
import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.gui.controller.reporter.Reporter;
import com.backend.taskHandling.PhotoTask;
import com.backend.taskHandling.Task;
import com.objectTemplates.User;
import com.bot.telegram.processes.*;
import com.bot.telegram.processes.Process;
import com.backend.ObjectHub;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;

import com.utils.TessUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
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
import java.util.concurrent.Future;

public class Bot extends TelegramLongPollingBot {

    private final BackendFacade facade;

    private List<String> shoppingList;

    private Reporter progressReporter;

    Map<Integer, User> allowedUsersMap;

    private static Logger logger = Main.getLogger();

    Map<Class, Process> processCache = new HashMap<>(10);

    public Bot(BackendFacade backendFacade) {
        this.facade = backendFacade;
        this.allowedUsersMap = facade.getAllowedUsers();
        shoppingList = facade.getShoppingList();

        progressReporter = new ProgressReporter() {
            @Override
            public void setTotalSteps(int steps, Update updateOrNull) {
                progressManager.setTotalSteps(steps);
                sendMsg("Start process " + allowedUsersMap.get(updateOrNull.getMessage().getFrom().getId()).getProcess().getProcessName(), updateOrNull, null, false, false);
            }

            @Override
            public void addStep(Update updateOrNull) {
                progressManager.addStep();
                sendMsg(progressManager.getCurrentProgress() + "%", updateOrNull, null, false, false);
            }

            @Override
            public void setStep(int step, Update updateOrNull) {
                progressManager.setCurrentStep(step);
                sendMsg(progressManager.getCurrentProgress() + "%", updateOrNull, null, false, false);
            }
        };
        setupProcessCache(facade, progressReporter);
    }

    private void setupProcessCache(BackendFacade facade, Reporter reporter) {
        ProgressReporter progressReporter = (ProgressReporter) reporter;
        processCache.put(BonProcess.class, new BonProcess(progressReporter, facade));
        processCache.put(CalenderProcess.class, new CalenderProcess(progressReporter, facade));
        processCache.put(GetBonsProcess.class, new GetBonsProcess(progressReporter, facade));
        processCache.put(GetPicsProcess.class, new GetPicsProcess(progressReporter, facade));
        processCache.put(MapQRItemProcess.class, new MapQRItemProcess(progressReporter, facade));
        processCache.put(MemoProcess.class, new MemoProcess(progressReporter, facade));
        processCache.put(NewUserRegProcess.class, new NewUserRegProcess(progressReporter, facade));
        processCache.put(RemoveLastProcess.class, new RemoveLastProcess(progressReporter, facade));
        processCache.put(ShoppingListProcess.class, new ShoppingListProcess(progressReporter, facade));
        processCache.put(StandardListProcess.class, new StandardListProcess(progressReporter, facade));
        processCache.put(StartProcess.class, new StartProcess(progressReporter, facade));
        processCache.put(SumProcess.class, new SumProcess(progressReporter, facade));
        processCache.put(FurtherOptionsProcess.class, new FurtherOptionsProcess(progressReporter, facade));
    }

    /**
     * Method for receiving messages.
     *
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        printUpdateData(update);
        int currentUserID;
        String userName;
        String textGivenByUser;
        if (update.hasCallbackQuery()) {
            currentUserID = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            textGivenByUser = update.getCallbackQuery().getData();
            if (textGivenByUser.equals("abort")) {
                abortProcess(update);
                return;
            }
        } else {
            currentUserID = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();
        }

        //Check if user is in System
        if (allowedUsersMap.get(currentUserID) == null) {
            allowedUsersMap.put(currentUserID, new User(currentUserID, userName, facade));
            sendMsg("Hallo " + userName + ", ich hab dich noch nicht im System gefunden, bitte gib das PW für NussBot ein:", update, null, true, false);
            allowedUsersMap.get(currentUserID).setProcess(processCache.get(NewUserRegProcess.class));
            return;
        }
        try {
            processUpdateReceveived(update);
        } catch (Exception e) {
            logger.error("Couldn't process update.", e);
        }
    }

    public void processUpdateReceveived(Update update) throws Exception {
        int currentUserID;
        String textGivenByUser;
        boolean isBusy = getNonBotUserFromUpdate(update).isBusy();
        if (update.hasCallbackQuery()) {
            currentUserID = update.getCallbackQuery().getFrom().getId();
            textGivenByUser = update.getCallbackQuery().getData();
        } else {
            textGivenByUser = update.getMessage().getText();
            currentUserID = update.getMessage().getFrom().getId();
        }

        Process process = allowedUsersMap.get(currentUserID).getProcess();
        if (checkBusy(update, isBusy, process)) return;

        try {
            if (process == null) {
                setupProcess(update, currentUserID);
            }//TODO zweimal abfrage nach null nötig da einige Prozesse zb StartProcess sich gleich wieder null setzen
            performProcess(update, currentUserID, textGivenByUser);
        } catch (Exception e) {
            logger.error("Something went wrong :(", e);
            throw new RuntimeException();
        }
    }

    private void performProcess(Update update, int currentUserID, String textGivenByUser) throws TelegramApiException {
        Process process = allowedUsersMap.get(currentUserID).getProcess();
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            sendMsg("Verarbeite Bild...", update, null, true, false);
            processPhoto(update);
        } else {
            process.performNextStep(textGivenByUser, update, this);
        }
    }

    private void setupProcess(Update update, int currentUserID) {
        //Set process if null
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            sendMsg("Verarbeite Bild...", update, null, true, false);
            processPhoto(update);
        } else {
            allowedUsersMap.get(currentUserID).setProcess(fetchProcessByUpdate(update));
        }
    }

    private boolean checkBusy(Update update, boolean isBusy, Process process) {
        if (isBusy) {
            Message message = sendMsg("Bin am arbeiten...", update, KeyboardFactory.KeyBoardType.Abort, true, true);
            process.getSentMessages().add(message);
            return true;
        }
        return false;
    }

    private void printUpdateData(Update update) {
        StringBuilder printBuilder;
        if (update.getMessage() == null) {
            printBuilder = new StringBuilder(LocalDateTime.now().toString() + ":    Update from " + update.getCallbackQuery().getFrom().getFirstName() + " ");
            printBuilder.append(update.getCallbackQuery().getData());
        } else {
            printBuilder = new StringBuilder(LocalDateTime.now().toString() + ":    Update from " + update.getMessage().getFrom().getFirstName() + " ");
            String append = update.getMessage().hasPhoto() ? ", new Picture" : "";
            printBuilder.append(append);
            append = update.getMessage().hasText() ? ", cmd: " + update.getMessage().getText() : " ";
            printBuilder.append(append);
        }
        logger.info(printBuilder.toString());
    }

    private void processPhoto(Update update) {
        Future photoFuture = ObjectHub.getInstance().getExecutorService().submit(() -> {
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

            if (facade.isFilePresent(largestPhoto)) {
                //Is File already stored...?
                logger.info("File already present: " + largestPhoto.getName());
                sendMsg("Bild schon vorhanden.", update, null, true, false);
                user.setBusy(false);
                process.reset(this, user);
                user.setProcess(null);
                return;
            }

            File targetFile = new File(ObjectHub.getInstance().getArchiver().getDocumentFolder(), LocalDateTime.now().toString().replace('.', '-').replace(':', '_') + filePath.replace("/", ""));
            try {
                FileUtils.copyFile(largestPhoto, targetFile);
            } catch (IOException e) {
                logger.error(largestPhoto.getAbsolutePath(), e);
            }

            Set<String> tags = null;
            if (update.getMessage().getCaption() != null && update.getMessage().getCaption().toLowerCase().startsWith("tag")) {
                tags = parseTags(update.getMessage().getCaption().replace("tag ", ""));
            }

            Document document = TessUtil.processFile(targetFile, update.getMessage().getFrom().getId(), tags, facade);
            try {
                if ((TessUtil.checkIfBon(document.getContent()) || process instanceof BonProcess)) {
                    float sum = TessUtil.getLastNumber(document.getContent());
                    Bon bon = new Bon(document, sum);
                    BonProcess bonProcess = (BonProcess) process;
                    bonProcess.setBon(bon);
                }
            } catch (Exception e) {
                logger.error(null, e);
            }

            if (process != null && process.getClass().equals(BonProcess.class)) {
                Message message = sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath(), "Das ist ein Bon oder?", KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Boolean, true, true, "isBon", facade));
                user.getProcess().getSentMessages().add(message);
            } else {
                sendMsg("Fertig.", update, null, true, false);
            }
            logger.info("Processed " + document.getOriginalFileName());
            user.setBusy(false);
        });

        Task photoAbortTask = new PhotoTask(allowedUsersMap.get(update.getMessage().getFrom().getId()), this, photoFuture, facade);
        ObjectHub.getInstance().getTasksRunnable().getTasksToDo().add(photoAbortTask);
    }

    private Set<String> parseTags(String input) {
        //Tags have to be input by Caption
        input = input.toLowerCase().replace("tag ", "");
        Set<String> tags = new HashSet<>();
        while (input.contains(",")) {
            String tag = input.substring(0, input.indexOf(','));
            tags.add(tag);
            input = input.replace(tag, "").replaceFirst(",", "");
        }

        tags.add(input);
        return tags;
    }

    public Message sendPhotoFromURL(Update update, String imagePath, String caption, ReplyKeyboard possibleKeyBoardOrNull) {
        SendPhoto sendPhoto = null;
        User user = getNonBotUserFromUpdate(update);
        Message message = null;
        try {
            sendPhoto = new SendPhoto().setPhoto("SomeText", new FileInputStream(new File(imagePath)));
            sendPhoto.setCaption(caption);
            if (possibleKeyBoardOrNull != null) {
                sendPhoto.setReplyMarkup(possibleKeyBoardOrNull);
                if (!(possibleKeyBoardOrNull instanceof InlineKeyboardMarkup)) {
                    //In case of an Inlinekeyboard it will not be stored as keyboardcontext
                    user.setKeyboardContext(possibleKeyBoardOrNull);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error(imagePath, e);
            return message;
        }
        long chatID = update.hasMessage() ? update.getMessage().getChatId() : (long) update.getCallbackQuery().getFrom().getId();
        sendPhoto.setChatId(chatID);
        try {
            message = execute(sendPhoto);
        } catch (TelegramApiException e) {
            user.setBusy(false);
            sendMsg("Fehler, Aktion abgebrochen.", update, null, true, false);
            logger.error(null, e);
        }
        return message;
    }

    public Message sendVideoFromURL(User user, String VideoPath, String caption) {
        SendVideo sendVideo = null;
        Message message = null;
        try {
            sendVideo = new SendVideo().setVideo("SomeText", new FileInputStream(new File(VideoPath)));
            sendVideo.setCaption(caption);
        } catch (FileNotFoundException e) {
            logger.error(VideoPath, e);
            return message;
        }
        long chatID = user.getId();
        sendVideo.setChatId(chatID);
        try {
            message = execute(sendVideo);
        } catch (TelegramApiException e) {
            logger.error(null, e);
        }
        return message;
    }

    public java.io.File downloadPhotoByFilePath(String filePath) {
        try {
            // Download the file calling AbsSender::downloadFile method
            return downloadFile(filePath);
        } catch (TelegramApiException e) {
            logger.error(filePath, e);
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
                logger.error(null, e);
            }
        }
        return null; // Just in case
    }

    private Process fetchProcessByUpdate(Update update) {
        String textGivenByUser = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : update.getMessage().getText();

        Process processToReturn = null;
        if (textGivenByUser != null) {
            Optional<Process> process = fetchProcessByCommand(textGivenByUser);
            if (process.isEmpty()) {
                logger.warn("No process found for " + textGivenByUser);
            } else {
                return process.get();
            }
        }
        return processToReturn;
    }

    /**
     * This method returns the bot's name, which was specified during registration.
     *
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return "NussBot";
    }

    /**
     * This method returns the bot's token for communicating with the telegram server
     *
     * @return the bot's token
     */
    @Override
    public String getBotToken() {
        return ObjectHub.getInstance().getProperties().getProperty("tgBotToken");
    }

    public User getNonBotUserFromUpdate(Update update) {
        int userId = update.hasCallbackQuery() ? update.getCallbackQuery().getFrom().getId() : update.getMessage().getFrom().getId();
        return allowedUsersMap.get(userId);
    }

    public void abortProcess(Update update) {
        User user = getNonBotUserFromUpdate(update);
        if (user.getProcess() != null) {
            user.setBusy(false);
            String processName = user.getProcess().getProcessName();
            logger.info("User " + user.getName() + " aborts " + user.getProcess().getProcessName() + " Process.");
            user.getProcess().reset(this, user);
            try {
                sendMsg(processName + " abgebrochen.", update, null, false, false);
                if (update.hasCallbackQuery()) {
                    sendAnswerCallbackQuery(processName + " abgebrochen.", false, update.getCallbackQuery());
                }
            } catch (TelegramApiException e) {
                logger.error("Abort done, messaging about abort failed.", e);
            }
        } else {
            try {
                simpleEditMessage("Abgebrochen", update, KeyboardFactory.KeyBoardType.NoButtons);
                if (update.hasCallbackQuery()) {
                    sendAnswerCallbackQuery("Abgebrochen", false, update.getCallbackQuery());
                }
            } catch (TelegramApiException e) {
                if (e.getMessage().equals("Error editing message reply markup")) {
                    logger.info("Message not edited.");
                } else {
                    logger.error(((TelegramApiRequestException) e).getApiResponse(), e);
                }
            }
        }
    }


    public Message sendKeyboard(String s, Update update, ReplyKeyboard replyKeyboard, boolean isReply) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(getMassageFromUpdate(update).getChatId());
        if (isReply) {
            sendMessage.setReplyToMessageId(getMassageFromUpdate(update).getMessageId());
        }
        sendMessage.setReplyMarkup(replyKeyboard);
        if (!(replyKeyboard instanceof InlineKeyboardMarkup)) {
            allowedUsersMap.get(getMassageFromUpdate(update).getFrom().getId()).setKeyboardContext(replyKeyboard);
        }
        sendMessage.setText(s);

        Message messageToReturn = null;
        try {
            messageToReturn = execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error(null, e);
        }
        return messageToReturn;
    }

    public Message askBoolean(String question, Update update, boolean isReply, String callbackPrefixOrNull) throws TelegramApiException {
        Message message = null;
        if (update.hasCallbackQuery()) {
            message = simpleEditMessage(question, update, KeyboardFactory.KeyBoardType.Boolean,callbackPrefixOrNull);
        } else {
            message = sendMsg(question, update, KeyboardFactory.KeyBoardType.Boolean, isReply, true);
        }
        return message;
    }

    public Message askMonth(String question, Update update, boolean isReply, String callbackPrefix) throws TelegramApiException {
        Message message = null;
        if (update.hasCallbackQuery()) {
            message = simpleEditMessage(question, update, KeyboardFactory.KeyBoardType.Calendar_Month, callbackPrefix);
        } else {
            message = sendMsg(question, update, KeyboardFactory.KeyBoardType.Calendar_Month, callbackPrefix, isReply, true);
        }
        return message;
    }

    public void editCaption(String text, Message message) {
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setChatId(message.getChatId() + "");
        editMessageCaption.setMessageId(message.getMessageId());
        editMessageCaption.setCaption(text);
        try {
            execute(editMessageCaption);
        } catch (TelegramApiException e) {
            logger.error("Couldn't edit caption.", e);
        }
    }

    //Convenience method to have one edit method for everything
    public Message simpleEditMessage(String text, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, String callBackPrefix) throws TelegramApiException {
        Message message = getMassageFromUpdate(update);
        return simpleEditMessage(text, message, keyBoardTypeOrNull, callBackPrefix);
    }

    public Message simpleEditMessage(String text, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull) throws TelegramApiException {
        Message message = getMassageFromUpdate(update);
        return simpleEditMessage(text, message, keyBoardTypeOrNull, "");
    }

    public Message simpleEditMessage(String text, Message message, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, String callbackPrefix) throws TelegramApiException {
        List<List<InlineKeyboardButton>> inputKeyboard = KeyboardFactory.createInlineKeyboard(keyBoardTypeOrNull, callbackPrefix, facade);
        return simpleEditMessage(text, message, inputKeyboard, callbackPrefix);
    }

    public Message simpleEditMessage(String text, Message message, List<List<InlineKeyboardButton>> inputKeyboard, String callbackPrefix) throws TelegramApiException {
        String prefix = callbackPrefix != null ? callbackPrefix : "";
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(inputKeyboard);
        return simpleEditMessage(text, message, inlineKeyboardMarkup, prefix);
    }

    public Message simpleEditMessage(String text, Message message, ReplyKeyboard inputKeyboard, String callbackPrefix) throws TelegramApiException {

        if (!message.hasText()) {
            if (message.hasPhoto() && message.getCaption() != null) {
                editCaption(text, message);
            }
        } else {
            if (tryEditText(text, message, (InlineKeyboardMarkup) inputKeyboard)) return message;
        }

        if (inputKeyboard != null && message.hasReplyMarkup()) {
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(message.getChatId());
            editMessageReplyMarkup.setMessageId(message.getMessageId());
            editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) inputKeyboard);
            try {
                execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                if (e.getMessage().equals("Error editing message reply markup")) {
                    logger.info("Couldn't change ReplyMarkup for 1 message.");
                    return message;
                }
                throw e;
            }
        }
        return message;
    }

    private boolean tryEditText(String text, Message message, InlineKeyboardMarkup inputKeyboard) throws TelegramApiException {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(message.getChatId());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(text);

        editMessageText.setReplyMarkup(inputKeyboard);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            if (e.getMessage().equals("Error editing message reply markup") || e.getMessage().contains("Error editing message text")) {
                logger.info("Couldn't change ReplyMarkup for 1 message.");
                return true;
            }
            throw e;
        }
        return false;
    }

    public Message askYear(String question, Update update, boolean isReply, String callbackPrefix) throws TelegramApiException {
        Message message = null;
        if (update.hasCallbackQuery()) {
            message = simpleEditMessage(question, update, KeyboardFactory.KeyBoardType.Calendar_Year, callbackPrefix);
        } else {
            message = sendMsg(question, update, KeyboardFactory.KeyBoardType.Calendar_Year, callbackPrefix, isReply, true);
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
     * Documents cannot be send in groups like pictures
     */
    public Message sendDocument(Update update, boolean isReply, InputMediaDocument inputMediaDocument) {
        Message message = getMassageFromUpdate(update);
        long chatID = message.getChatId();
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(inputMediaDocument.getMediaFile());
        sendDocument.setChatId(chatID);
        Message messageToReturn = null;
        if (isReply) {
            sendDocument.setReplyToMessageId(message.getMessageId());
        }
        try {
            messageToReturn = execute(sendDocument);
        } catch (TelegramApiException e) {
            logger.error("Failed to send Document message.", e);
        }
        return messageToReturn;
    }

    public synchronized List<Message> sendMediaMsg(Update update, boolean isReply, List<InputMedia> inputMediaList) throws TelegramApiException {
        Message message = getMassageFromUpdate(update);
        long chatID = message.getChatId();

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setMedia(inputMediaList);
        sendMediaGroup.setChatId(chatID);
        if (isReply) {
            sendMediaGroup.setReplyToMessageId(message.getMessageId());
        }
        List<Message> messageToReturn = null;
            if (inputMediaList.isEmpty()) {
                throw new TelegramApiException("No media in List found.");
            }
            messageToReturn = execute(sendMediaGroup);
        return messageToReturn;
    }

    public synchronized Message sendMsg(String s, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, boolean isReply, boolean inlineKeyboard) {
        return sendMsg(s, update, keyBoardTypeOrNull, "", isReply, inlineKeyboard, ParseMode.None);
    }

    public synchronized Message sendMsg(String s, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, boolean isReply, boolean inlineKeyboard, ParseMode parseModeOrNull) {
        return sendMsg(s, update, keyBoardTypeOrNull, "", isReply, inlineKeyboard, parseModeOrNull);
    }

    public synchronized Message sendMsg(String s, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, String callbackValuePrefix, boolean isReply, boolean inlineKeyboard) {
        return sendMsg(s, update, keyBoardTypeOrNull, callbackValuePrefix, isReply, inlineKeyboard, ParseMode.None);
    }

    public synchronized Message sendMsg(String s, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, String callbackValuePrefix, boolean isReply, boolean inlineKeyboard, ParseMode parseModeOrNull) {
        ReplyKeyboard replyKeyboard = null;
        if (keyBoardTypeOrNull != null) {
            replyKeyboard = KeyboardFactory.getKeyBoard(keyBoardTypeOrNull, inlineKeyboard, false, callbackValuePrefix, facade);
        }
        return sendMsg(s, update, replyKeyboard, callbackValuePrefix, isReply, inlineKeyboard, parseModeOrNull);
    }

    public synchronized Message sendMsg(String s, Update update, ReplyKeyboard replyKeyboard, String callbackValuePrefix, boolean isReply, boolean inlineKeyboard, ParseMode parseModeOrNull) {
        Message message = getMassageFromUpdate(update);
        long chatID = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID);
        if (isReply) {
            sendMessage.setReplyToMessageId(message.getMessageId());
        }
        if (parseModeOrNull != null && parseModeOrNull != ParseMode.None) {
            sendMessage.setParseMode(parseModeOrNull.name());
        }
        if (replyKeyboard != null) {
            sendMessage.setReplyMarkup(replyKeyboard);
            if (!inlineKeyboard) {
                //If no InlineKeyboard set the keyboardcontext to the incoming keyboard. Therefore making sure the list processes get the certain keyboards as context.
                allowedUsersMap.get(update.getMessage().getFrom().getId()).setKeyboardContext(replyKeyboard);
            }
        }
        sendMessage.setText(s);
        Message messageToReturn = null;
        try {
            messageToReturn = execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message.", e);
        }
        return messageToReturn;
    }

    public synchronized Message sendSimpleMsg(String s, long chatID, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, boolean inlineKeyboard, String callbackPrefixOrNull) {
        boolean isOneTimeKeyboard = false;
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID);
        String callbackValue = callbackPrefixOrNull == null ? ";" : callbackPrefixOrNull;
        if (keyBoardTypeOrNull != null) {
            ReplyKeyboard replyKeyboard = KeyboardFactory.getKeyBoard(keyBoardTypeOrNull, inlineKeyboard, isOneTimeKeyboard, callbackValue, facade);
            sendMessage.setReplyMarkup(replyKeyboard);
            if (!inlineKeyboard) {
                //If no InlineKeyboard set the keyboardcontext to the incoming keyboard. Therefore making sure the list processes get the certain keyboards as context.
                allowedUsersMap.get((int) chatID).setKeyboardContext(replyKeyboard);
            }
        }
        sendMessage.setText(s);
        Message messageToReturn = null;
        try {
            messageToReturn = execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message. (ChatId: )" + chatID != null ? chatID : "unknown.\n Message: " + s, e);
        }
        return messageToReturn;
    }

    public Message getMassageFromUpdate(Update update) {
        return update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() : update.getMessage();
    }

    public void sendAnswerCallbackQuery(String text, boolean alert, CallbackQuery callbackquery) throws TelegramApiException {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackquery.getId());
        answerCallbackQuery.setShowAlert(alert);
        answerCallbackQuery.setText(text);
        execute(answerCallbackQuery);
    }

    private Optional<Process> fetchProcessByCommand(String cmd) {
        for (Map.Entry<Class, Process> entry : processCache.entrySet()) {
            if (entry.getValue().hasCommand(cmd)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    //GETTER SETTER


    public Map<Integer, User> getAllowedUsersMap() {
        return allowedUsersMap;
    }

    public void setAllowedUsersMap(Map<Integer, User> allowedUsersMap) {
        this.allowedUsersMap = allowedUsersMap;
    }

    public List<String> getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(List<String> shoppingList) {
        this.shoppingList = shoppingList;
    }

    public enum ParseMode { //https://core.telegram.org/bots/api#formatting-options
        None, Markdown, HTML
    }

}
