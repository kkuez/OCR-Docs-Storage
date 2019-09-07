package com;

import com.Controller.MainController;
import com.Misc.CustomProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectHub {

    private static ObjectHub instance = null;

    private MainController mainController;

    private ExecutorService executorService;

    private CustomProperties properties;

    private ObjectHub() {
        properties = new CustomProperties();
        String root = "";
        try {
            properties.load(new FileInputStream(root + "setup.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        archiver = new Archiver();
        executorService = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("threads")));
    }

    private Archiver archiver;

    public static ObjectHub getInstance() {
        if (instance == null) {
            instance = new ObjectHub();
        }
        return instance;
    }

    // GETTER SETTER

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

}
