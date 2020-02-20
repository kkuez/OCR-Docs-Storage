package com;

import com.controller.StartApplication;
import com.telegram.Bot;

import com.network.ListenerThread;
import javafx.application.Application;
import org.apache.log4j.*;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;


public class Main {

    private static Logger logger;

    public static void main(String[] args) {
        // write your code here
        logger = createLogger();

        logger.info("\n\nStarting.");
        for(String s : args){
            if(s.equals("-gui")){
                launchGui(args);
            }
            if(s.equals("-bot")){
                Bot bot = null;
                while(bot == null){
                    try {
                        bot = activateTGBot(null);
                        ListenerThread listenerThread = new ListenerThread(bot);
                        listenerThread.start();
                    } catch (TelegramApiRequestException e) {
                        logger.error("Couldnt start bot", e);
                        System.exit(2);
                    }
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
            logger.error("Failed activating bot", e);
            throw e;
        }
        logger.info("System: Activated Bot");
        return bot;
    }

    private static String getLogFile(){
        Properties properties = new Properties();
        File propertiesFile = new File(".", "setup.properties");
        try {
            System.out.println("Properties: " + propertiesFile.getCanonicalPath());
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        File monthFolder = new File(properties.getProperty("pathToProjectFolder") + File.separator + "Archiv",  LocalDate.now().getMonth().toString() + "_" + LocalDate.now().getYear());
        if(!monthFolder.exists()){
            monthFolder.mkdir();
        }
        File logFolder = new File(monthFolder, "Logs");
        if(!logFolder.exists()){
            logFolder.mkdir();
        }
        File logFile = new File(logFolder, LocalDate.now().toString().replace(".", "-").replace(":", "_") + ".log");
        if(!logFile.exists()){
            try {
                boolean logFileSuccess = logFile.createNewFile();
                if(!logFileSuccess){
                    throw new RuntimeException("Failed to create logFile.");
                }
                System.out.println("Logfile: " + logFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(2);
            }catch (RuntimeException ex) {
                ex.printStackTrace();
                System.exit(2);
            }
        }
        return logFile.getAbsolutePath();
    }

    private static Logger createLogger(){
        Logger logger = Logger.getLogger(Main.class);
        PatternLayout layout = new PatternLayout("%d{HH:mm:ss.SSS} [%t] \t%m%n");
        logger.addAppender(new ConsoleAppender(layout));
        FileAppender logFileAppender = null;
        try {
            logFileAppender = new FileAppender(layout, getLogFile(), true);
        } catch (IOException e) {
            logger.error("Failed activating bot", e);
            System.exit(2);
        }
        logger.addAppender(logFileAppender);
        return logger;
    }
        private static void launchGui(String[] args){
            Application.launch(StartApplication.class, args);
        }

        public static Logger getLogger(){
        return logger;
        }
}
