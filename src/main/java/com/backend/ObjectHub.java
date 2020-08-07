package com.backend;

import com.Main;
import com.TasksRunnable;
import com.gui.controller.MainController;
import com.objectTemplates.User;
import com.bot.telegram.Bot;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectHub {

    private static ObjectHub instance = null;

    private static Logger logger = Main.getLogger();

    private MainController mainController;

    private ExecutorService executorService;

    private CustomProperties properties;

    private Map<Integer, User> allowedUsersMap;

    private Bot bot;

    private TasksRunnable tasksRunnable;

    private BackendFacade facade;

    private ObjectHub() {
        properties = new CustomProperties();
        String root = "";

        try {
            properties.load(new FileInputStream(root + "setup.properties"));
        } catch (IOException e) {
            logger.error("Failed activating bot", e);
        }

        File dbFile = new File(getProperties().getProperty("dbPath"));
        facade = new FacadeController(dbFile);
        archiver = new Archiver(properties);

        executorService = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("threads")));
    }

    public void initLater(){
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.error("Couldn't initialize ObjectHub.", e);
                Thread.currentThread().interrupt();
                System.exit(2);
            }
            allowedUsersMap = facade.getAllowedUsers();
            tasksRunnable = new TasksRunnable();
            tasksRunnable.setBot(getBot());
            tasksRunnable.setFacade(facade);
            tasksRunnable.run();
        });
        thread.setName("TasksToDoThread");
        thread.start();
    }
    private Archiver archiver;

    public static ObjectHub getInstance() {
        if (instance == null) {
            instance = new ObjectHub();
        }
        return instance;
    }

    // GETTER SETTER
    public TasksRunnable getTasksRunnable() {
        return tasksRunnable;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }
    public Map<Integer, User> getAllowedUsersMap() {
        return allowedUsersMap;
    }

    public void setAllowedUsersMap(Map<Integer, User> allowedUsersMap) {
        this.allowedUsersMap = allowedUsersMap;
    }
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(CustomProperties properties) {
        this.properties = properties;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
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

    public BackendFacade getFacade() {
        return facade;
    }
}
