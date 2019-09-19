package com.Telegram.Processes;

import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.BotUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartProcess extends Process {

    public StartProcess(Bot bot, Update update){
        BotUtil.sendKeyBoard("Wähle eine Aktion :)",bot, update, KeyboardFactory.KeyBoardType.Start);
        getBot().process = null;
    }

    @Override
    public void performNextStep(String arg, Update update) {

    }
}
