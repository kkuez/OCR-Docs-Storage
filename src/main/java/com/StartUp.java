package com;

import com.backend.BackendFacade;
import com.backend.ObjectHub;
import com.backend.network.ListenerThread;
import com.backend.taskhandling.TaskFactory;
import com.bot.telegram.Bot;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;

import static com.utils.PinUtil.setGPIO;

@Service
public class StartUp {

    private static Logger logger;

    private BackendFacade facade;
    private TasksRunnable tasksRunnable;
    private TaskFactory taskFactory;

    @Lazy
    public StartUp(ObjectHub objectHub, BackendFacade facade, TasksRunnable tasksRunnable, TaskFactory taskFactory) {
        this.facade = facade;
        this.tasksRunnable = tasksRunnable;
        this.taskFactory = taskFactory;
        logger = createLogger();
        logger.info("\n\nStarting.");
        Bot bot = null;
        while (bot == null) {
            bot = activateTGBot(null, objectHub);
            taskFactory.setBot(bot);
            ListenerThread listenerThread = new ListenerThread(bot, facade);
            listenerThread.start();
        }
    }

    private Bot activateTGBot(Bot inputBotOrNull, ObjectHub objectHub) {
        System.out.println("Trying to initialize Telegram-Bot...");
        ApiContextInitializer.init();
        Bot bot = null;
        bot = inputBotOrNull == null ? new Bot(facade, objectHub, tasksRunnable) : inputBotOrNull;
        objectHub.setBot(bot);
        objectHub.initLater(tasksRunnable);
        BotSession botSession = null;
        while (botSession == null) {
            try {
                TelegramBotsApi telegramBotApi = new TelegramBotsApi();
                botSession = telegramBotApi.registerBot(bot);
                setGPIO(0, objectHub);
            } catch (TelegramApiRequestException e) {
                logger.error("Failed registering bot.\nTrying again in 30 seconds...", e);
                setGPIO(1,objectHub);
                pause(30);
            }
        }
        logger.info("System: Activated Bot");
        return bot;
    }

    private void pause(int seconds) {
        try {
            Thread.sleep((seconds * 1000));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private String getLogFile() {
        Properties properties = new Properties();
        File propertiesFile = new File(".", "setup.properties");
        try {
            System.out.println("Properties: " + propertiesFile.getCanonicalPath());
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        File monthFolder = new File(properties.getProperty("localArchivePath"), LocalDate.now().getMonth().toString() + "_" + LocalDate.now().getYear());
        if (!monthFolder.exists()) {
            monthFolder.mkdir();
        }
        File logFolder = new File(monthFolder, "Logs");
        if (!logFolder.exists()) {
            logFolder.mkdir();
        }
        File logFile = new File(logFolder, LocalDate.now().toString().replace(".", "-").replace(":", "_") + ".log");
        if (!logFile.exists()) {
            try {
                boolean logFileSuccess = logFile.createNewFile();
                if (!logFileSuccess) {
                    throw new RuntimeException("Failed to create logFile.");
                }
                System.out.println("Logfile: " + logFile.getAbsolutePath());
            } catch (IOException e) {
                //No Logger since this is the algorythm to initilialize logger.
                e.printStackTrace();
                System.exit(2);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                System.exit(2);
            }
        }
        return logFile.getAbsolutePath();
    }

    private Logger createLogger() {
        Logger logger = Logger.getLogger(StartUp.class);
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

    public static Logger getLogger() {
        return logger;
    }
}
