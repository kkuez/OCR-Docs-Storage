package com.backend;

import com.TasksRunnable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ObjectHub {

    private static Logger logger = Logger.getLogger(ObjectHub.class);

    private ExecutorService executorService;

    private CustomProperties properties;

    @Autowired
    ObjectHub(Archiver archiver) {
        properties = new CustomProperties();
        String root = "";

        try {
            properties.load(new FileInputStream(root + "setup.properties"));
        } catch (IOException e) {
            logger.error("Failed activating bot", e);
        }

        this.archiver = archiver;
        executorService = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("threads")));
    }

    public void initLater(TasksRunnable tasksRunnable) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.error("Couldn't initialize ObjectHub.", e);
                Thread.currentThread().interrupt();
                System.exit(2);
            }
            tasksRunnable.run();
        });
        thread.setName("TasksToDoThread");
        thread.start();
    }

    private Archiver archiver;


    // GETTER SETTER
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(CustomProperties properties) {
        this.properties = properties;
    }

    public Archiver getArchiver() {
        return archiver;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
