package com;

import com.Controller.StartApplication;
import com.Telegram.Bot;
import javafx.application.Application;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {

    public static void main(String[] args) {
        // write your code here
        ObjectHub.getInstance().setInputArgs(args);


        for(String s : args){
            if(s.equals("-gui")){
                launchGui(args);
            }
            if(s.equals("-bot")){
                activateTGBot();
            }
        }
        }

        private static void launchGui(String[] args){
            Application.launch(StartApplication.class, args);
        }

    private static void activateTGBot(){
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotApi = new TelegramBotsApi();
        try {
            Bot bot = new Bot();
            ObjectHub.getInstance().setBot(bot);
            telegramBotApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
