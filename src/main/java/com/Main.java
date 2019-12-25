package com;

import com.controller.StartApplication;
import com.telegram.Bot;
import com.utils.LogUtil;
import com.network.ListenerThread;
import javafx.application.Application;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {

    public static void main(String[] args) {
        // write your code here

        for(String s : args){
            if(s.equals("-gui")){
                launchGui(args);
            }
            if(s.equals("-bot")){
                Bot bot = null;
                while(bot == null){
                    try {
                        bot = activateTGBot(null);
                    } catch (TelegramApiRequestException e) {
                        e.printStackTrace();
                        LogUtil.log("Waiting 30s and try again...");
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    ListenerThread listenerThread = new ListenerThread(bot);
                    listenerThread.start();
                }
            }
        }
        }
    private static Bot activateTGBot(Bot inputBotOrNull) throws TelegramApiRequestException {
        Bot bot = null;
        try {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotApi = new TelegramBotsApi();
            bot = inputBotOrNull == null ? new Bot() : inputBotOrNull;
            ObjectHub.getInstance().setBot(bot);
            ObjectHub.getInstance().initLater();
            telegramBotApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            LogUtil.logError(null, e);
            LogUtil.log("Failed activating ");
            throw e;
        }
        LogUtil.log("System: Activated Bot");
        return bot;
    }


        private static void launchGui(String[] args){
            Application.launch(StartApplication.class, args);
        }
}
