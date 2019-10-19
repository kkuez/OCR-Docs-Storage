package com.Utils;

import com.ObjectHub;
import com.Telegram.KeyboardFactory;
import com.Telegram.Bot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;

public class BotUtil {

    public static Message sendKeyboard(String s, Bot bot, Update update, ReplyKeyboard replyKeyboard, boolean isReply){
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
            messageToReturn =  bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
        return messageToReturn;
    }

    public static boolean activateTGBot(Bot inputBotOrNull) throws TelegramApiRequestException {
        LogUtil.log("System: Activate Bot");
        Bot bot = null;
        try {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotApi = new TelegramBotsApi();
            bot = inputBotOrNull == null ? new Bot() : inputBotOrNull;
            ObjectHub.getInstance().setBot(bot);
            telegramBotApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            LogUtil.logError(null, e);
            LogUtil.log("Failed activating Bot.");
            throw e;
        }
        return true;
    }

    public static void askBoolean(String question, Update update, Bot bot, boolean isReply){
        if(update.hasCallbackQuery()){
            simpleEditMessage(question, bot, update, KeyboardFactory.KeyBoardType.Boolean);
        }else{
            sendMsg(question, bot, update, KeyboardFactory.KeyBoardType.Boolean, isReply, true);
        }
    }
public static void askMonth(String question, Update update, Bot bot, boolean isReply) {
    if (update.hasCallbackQuery()) {
        simpleEditMessage(question, bot, update, KeyboardFactory.KeyBoardType.Calendar_Month);
    } else {
        sendMsg(question, bot, update, KeyboardFactory.KeyBoardType.Calendar_Month, isReply, true);
    }
}
    public static void editCaption(String text, Bot bot, Message message){
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setChatId(message.getChatId() + "");
        editMessageCaption.setMessageId(message.getMessageId());
        editMessageCaption.setCaption(text);
        try {
            bot.execute(editMessageCaption);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void editMessage(String text, Bot bot, Message message){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(message.getChatId());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(text);
        try {
            bot.execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //Convenience method to have one edit method for everything
    public static void simpleEditMessage(String text, Bot bot, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull){
        Message message = getMassageFromUpdate(update);
        simpleEditMessage(text, bot, message, keyBoardTypeOrNull);
    }
    public static void simpleEditMessage(String text, Bot bot, Message message, KeyboardFactory.KeyBoardType keyBoardTypeOrNull){

        if(!message.hasText()){
            if(message.hasPhoto() && message.getCaption() != null){
                editCaption(text, bot, message);
            }
        }else{
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(message.getChatId());
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setText(text);
            try {
                bot.execute(editMessageText);
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
                    bot.execute(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
    }}

    public static void askYear(String question, Update update, Bot bot, boolean isReply){
        if (update.hasCallbackQuery()) {
            simpleEditMessage(question, bot, update, KeyboardFactory.KeyBoardType.Calendar_Year);
        } else {
            sendMsg(question, bot, update, KeyboardFactory.KeyBoardType.Calendar_Year, isReply, true);
        }
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
    public static Message sendDocument(Bot bot, Update update,  boolean isReply,  InputMediaDocument inputMediaDocument){
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
            messageToReturn = bot.execute(sendDocument);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
        return messageToReturn;
    }

    public static synchronized List<Message> sendMediaMsg(Bot bot, Update update,  boolean isReply,  List<InputMedia> inputMediaList) {
        if(inputMediaList.size() == 0){
            sendMsg("Keine Dokumente gefunden für den Begriff.", bot, update, null, false, false);
            bot.abortProcess(update, ObjectHub.getInstance().getAllowedUsersMap(), update.getMessage().getFrom().getId());
            return new ArrayList<>();
        }
        Message message = getMassageFromUpdate(update);
        long chatID = message.getChatId();
        List<InputMedia> toBeRemovedList = new ArrayList<>();
        for(InputMedia inputMedia : inputMediaList){
            if(inputMedia instanceof InputMediaDocument){
                sendDocument(bot, update, true, (InputMediaDocument) inputMedia);
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
            messageToReturn = bot.execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
            sendMsg("Zuviele Dokumente gefunden für den Begriff... Abgebrochen.", bot, update, null, false, false);
            bot.abortProcess(update, ObjectHub.getInstance().getAllowedUsersMap(), update.getMessage().getFrom().getId());
        }

        return messageToReturn;
    }
    public static synchronized Message sendMsg(String s, Bot bot, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, boolean isReply, boolean inlineKeyboard) {
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
           messageToReturn = bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
        return messageToReturn;
    }

    public static Message getMassageFromUpdate(Update update){
       return update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() : update.getMessage();
    }

    public static  void sendAnswerCallbackQuery(String text, Bot bot, boolean alert, CallbackQuery callbackquery) throws TelegramApiException{
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackquery.getId());
        answerCallbackQuery.setShowAlert(alert);
        answerCallbackQuery.setText(text);
        bot.execute(answerCallbackQuery);
    }
}
