package com;

import com.Controller.StartApplication;
import com.Telegram.Bot;
import com.Utils.LogUtil;
import javafx.application.Application;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {

    public static void main(String[] args) {
        // write your code here
        ObjectHub.getInstance().setInputArgs(args);

        for(String s : args){
            if(s.equals("-gui")){
                launchGui(args);
            }
            if(s.equals("-bot")){
                boolean successfullyActivated = false;
                while(!successfullyActivated){
                    try {
                        successfullyActivated = activateTGBot(null);
                    } catch (TelegramApiRequestException e) {
                        e.printStackTrace();
                        LogUtil.log("Waiting 30s and try again...");
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        }
    private static boolean activateTGBot(Bot inputBotOrNull) throws TelegramApiRequestException {
        LogUtil.log("System: Activate Bot");
        Bot bot = null;
        try {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotApi = new TelegramBotsApi();
            bot = inputBotOrNull == null ? new Bot(ObjectHub.getInstance().getAllowedUsersMap()) : inputBotOrNull;
            ObjectHub.getInstance().setBot(bot);
            telegramBotApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            LogUtil.logError(null, e);
            LogUtil.log("Failed activating ");
            throw e;
        }
        return true;
    }


        private static void launchGui(String[] args){
            Application.launch(StartApplication.class, args);
        }
}
