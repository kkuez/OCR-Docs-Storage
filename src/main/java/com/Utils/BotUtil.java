package com.Utils;

import com.ObjectHub;
import com.ObjectTemplates.User;
import com.Telegram.KeyboardFactory;
import com.Telegram.Bot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Map;

public class BotUtil {

    public static void activateTGBot(Bot inputBotOrNull, Map<Integer, User> allowedUsersMap){
        LogUtil.log("System: Activate TG-Bot");
        Bot bot = null;
        try {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotApi = new TelegramBotsApi();
            bot = inputBotOrNull == null ? new Bot(allowedUsersMap) : inputBotOrNull;
            ObjectHub.getInstance().setBot(bot);
            telegramBotApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            LogUtil.logError(null, e);
        }
    }

    public static void askBoolean(String question, Update update, Bot bot){
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup keyboardMarkup = KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Boolean);
        sendMessage.setText(question);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
    }
public static void askMonth(String question, Update update, Bot bot){
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup keyboardMarkup = KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Calendar_Month);
        sendMessage.setText(question);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
    }

public static void askYear(String question, Update update, Bot bot){
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup keyboardMarkup = KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Calendar_Year);
        sendMessage.setText(question);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
    }

    /**
     * Method for creating a message and sending it.
     * @param chatId chat id
     * @param s The String that you want to send as a message.
     */
    public static synchronized void sendMsg(String chatId, String s, Bot bot) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
    }

    public static void sendKeyBoard(String message, Bot bot, Update update, KeyboardFactory.KeyBoardType keyBoardType){
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup keyboardMarkup = KeyboardFactory.getKeyBoard(keyBoardType);
        sendMessage.setText(message);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            LogUtil.logError(null, e);
        }
    }

}
