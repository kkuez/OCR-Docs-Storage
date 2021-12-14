package com;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.backend.BackendFacade;
import com.backend.CustomProperties;
import com.backend.taskhandling.TaskFactory;


@Service
public class StartUp {

    private static Logger logger;
    private static FileAppender logFileAppender;
    private static ConsoleAppender consoleAppender;
    private BackendFacade facade;
    private TasksRunnable tasksRunnable;
    private TaskFactory taskFactory;

    @Lazy
    public StartUp(BackendFacade facade, TasksRunnable tasksRunnable, TaskFactory taskFactory) {
        this.facade = facade;
        this.tasksRunnable = tasksRunnable;
        this.taskFactory = taskFactory;
        logger = createLogger(StartUp.class);
        logger.info("\n\nStarting.");
        facade.getObjectHub().initLater(tasksRunnable);
    }

    private static String getLogFile() {

        File monthFolder = new File(new CustomProperties().getProperty("localArchivePath"), LocalDate.now().getMonth().toString() + "_" + LocalDate.now().getYear());
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

    public static Logger createLogger(Class clazz) {
        return LogManager.getLogger(clazz);
    }

    public static Logger getLogger() {
        return logger;
    }
}
