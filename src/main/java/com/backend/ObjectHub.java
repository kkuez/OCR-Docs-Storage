package com.backend;

import com.StartUp;
import com.TasksRunnable;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ObjectHub {

    private static Logger logger = StartUp.createLogger(ObjectHub.class);

    private ExecutorService executorService;

    private CustomProperties properties;

    @Autowired
    public ObjectHub(Archiver archiver, CustomProperties properties) {
        this.properties = properties;
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
