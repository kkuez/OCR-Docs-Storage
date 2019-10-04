package com;

import com.Controller.StartApplication;
import com.Utils.BotUtil;
import com.Utils.LogUtil;
import javafx.application.Application;
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
