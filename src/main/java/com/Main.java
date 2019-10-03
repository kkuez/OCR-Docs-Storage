package com;

import com.Controller.StartApplication;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.LogUtil;
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
                boolean successfullyActivated = false;
                while(!successfullyActivated){
                    try {
                        successfullyActivated = BotUtil.activateTGBot(null);
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

        private static void launchGui(String[] args){
            Application.launch(StartApplication.class, args);
        }


}
