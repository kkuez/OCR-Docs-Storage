package com;

import com.backend.BackendFacade;
import com.backend.CustomProperties;
import com.backend.ObjectHub;
import com.backend.taskhandling.TaskFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

@Service
public class StartUp {

    private static Logger logger;
    private final CustomProperties properties;
    private BackendFacade facade;
    private TasksRunnable tasksRunnable;
    private TaskFactory taskFactory;

    @Lazy
    public StartUp(ObjectHub objectHub, BackendFacade facade, TasksRunnable tasksRunnable, TaskFactory taskFactory,
                   CustomProperties properties) throws NoSuchAlgorithmException, IOException {
        this.properties = properties;
        this.facade = facade;
        this.tasksRunnable = tasksRunnable;
        this.taskFactory = taskFactory;
        logger = createLogger();
        logger.info("\n\nStarting.");
    }

    private void startUp(ObjectHub objectHub) {
        System.out.println("Trying to initialize Telegram-Bot...");
        ApiContextInitializer.init();
        objectHub.initLater(tasksRunnable);
        BotSession botSession = null;
    }

    private String getLogFile() {
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
