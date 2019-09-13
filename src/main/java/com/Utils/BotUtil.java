package com.Utils;

import com.Telegram.Bot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class BotUtil {

    public static void askBoolean(String question, Update update, Bot bot){
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup keyboardMarkup = getBooleanKeyBoard();
        sendMessage.setText(question);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
            //sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static ReplyKeyboardMarkup getBooleanKeyBoard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Japp");
        keyboardFirstRow.add("Nee");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
}
