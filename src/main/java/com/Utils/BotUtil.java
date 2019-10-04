package com.Utils;

import com.ObjectHub;
import com.ObjectTemplates.User;
import com.Telegram.KeyboardFactory;
import com.Telegram.Bot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.List;
import java.util.Map;

public class BotUtil {
    public static void sendKeyboard(String s, Bot bot, Message message, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, boolean isReply, boolean isInlineKeyBoard, boolean isOnelineKeyboard){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        if(isReply){
            sendMessage.setReplyToMessageId(message.getMessageId());
        }
        if(keyBoardTypeOrNull != null){
            sendMessage.setReplyMarkup(KeyboardFactory.getKeyBoard(keyBoardTypeOrNull, isInlineKeyBoard, isOnelineKeyboard));
        }
        sendMessage.setText(s);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
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
        sendMsg(question, bot, update, KeyboardFactory.KeyBoardType.Boolean, isReply, true);
    }
public static void askMonth(String question, Update update, Bot bot, boolean isReply){
        sendMsg(question, bot, update, KeyboardFactory.KeyBoardType.Calendar_Month, isReply,true);
    }

public static void askYear(String question, Update update, Bot bot, boolean isReply){
    sendMsg(question, bot, update, KeyboardFactory.KeyBoardType.Calendar_Year, isReply, true);
    }

    /**
     * Method for creating a message and sending it.
     * @param chatId chat id
     * @param s The String that you want to send as a message.
     *
     */

    public static synchronized void sendMediaMsg(Bot bot, Update update,  boolean isReply,  List<InputMedia> inputMediaList) {
        boolean isOneTimeKeyboard = false;
        long chatID = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        Message message = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() : update.getMessage();

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setMedia(inputMediaList);
        sendMediaGroup.setChatId(chatID);
        if(isReply){
            sendMediaGroup.setReplyToMessageId(message.getMessageId());
        }
        try {
            bot.execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
    }
    public static synchronized void sendMsg(String s, Bot bot, Update update, KeyboardFactory.KeyBoardType keyBoardTypeOrNull, boolean isReply, boolean inlineKeyboard) {
        boolean isOneTimeKeyboard = false;
        long chatID = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        Message message = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() : update.getMessage();

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
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
    }
}
