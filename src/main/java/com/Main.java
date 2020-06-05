package com;

import com.controller.StartApplication;
import com.misc.taskHandling.CheckConnectionTask;
import com.network.ListenerThread;
import com.telegram.Bot;
import javafx.application.Application;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotSession;

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
                    bot = activateTGBot(null);
                    ListenerThread listenerThread = new ListenerThread(bot);
                    listenerThread.start();
                }
            }
        }
    }

    private static Bot activateTGBot(Bot inputBotOrNull) {
        System.out.println("Trying to initialize Telegram-Bot...");
        ApiContextInitializer.init();
        Bot bot = null;
        bot = inputBotOrNull == null ? new Bot() : inputBotOrNull;
        ObjectHub.getInstance().setBot(bot);
        ObjectHub.getInstance().initLater();
        BotSession botSession = null;
        while(botSession == null) {
            try {
                TelegramBotsApi telegramBotApi = new TelegramBotsApi();
                botSession = telegramBotApi.registerBot(bot);
                try {
                    CheckConnectionTask.setGPIO(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (TelegramApiRequestException e) {
                logger.error("Failed registering bot.\nTrying again in 30 seconds...", e);
                try {
                    CheckConnectionTask.setGPIO(1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                pause(30);
            }
        }
        logger.info("System: Activated Bot");
        return bot;
    }

    private static void pause(int seconds) {
        try {
            Thread.sleep((seconds * 1000));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
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
                //No Logger since this is the algorythm to initilialize logger.
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
            logger.error("Failed initializing logger", e);
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
