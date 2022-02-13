package com;

import com.backend.BackendFacade;
import com.backend.CustomProperties;
import com.backend.taskhandling.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class StartUp {

    private static Logger logger;
    private final BackendFacade facade;
    private final TasksRunnable tasksRunnable;
    private final TaskFactory taskFactory;

    @Lazy
    public StartUp(BackendFacade facade, TasksRunnable tasksRunnable, TaskFactory taskFactory) {
        this.facade = facade;
        this.tasksRunnable = tasksRunnable;
        this.taskFactory = taskFactory;
        logger = LoggerFactory.getLogger(StartUp.class);
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
}
