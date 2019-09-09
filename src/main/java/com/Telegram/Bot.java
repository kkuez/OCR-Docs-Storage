package com.Telegram;


import com.ObjectHub;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot {
    private TelegramLongPollingBot telegramLongPollingBot;

    public Bot(){
        ApiContextInitializer.init();
        telegramLongPollingBot = new TelegramLongPollingBot() {
            @Override
            public void onUpdateReceived(Update update) {
                String message = update.getMessage().getText();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(update.getMessage().getChatId().toString());
                sendMessage.setText(":)");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String getBotUsername() {
                return "NussigerBot";
            }

            @Override
            public String getBotToken() {
                return ObjectHub.getInstance().getProperties().getProperty("tgBotToken");
            }
        };
    }

}
