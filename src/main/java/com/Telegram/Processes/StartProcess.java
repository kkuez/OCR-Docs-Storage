package com.Telegram.Processes;

import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartProcess extends Process {

    public StartProcess(Bot bot, Update update){
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup keyboardMarkup = KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Start);
        sendMessage.setText("Wähle eine Aktion :)");
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        getBot().process = null;
    }

    @Override
    public void performNextStep(String arg, Update update) {

    }
}
