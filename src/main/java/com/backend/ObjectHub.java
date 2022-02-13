package com.backend;

import com.TasksRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ObjectHub {

    private static final Logger logger = LoggerFactory.getLogger(ObjectHub.class);

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

    private final Archiver archiver;


    // GETTER SETTER
    public Properties getProperties() {
        return properties;
    }

}
