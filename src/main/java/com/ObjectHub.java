package com;

import com.Controller.MainController;
import com.Misc.CustomProperties;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.DBUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectHub {

    private static ObjectHub instance = null;

    private MainController mainController;

    private ExecutorService executorService;

    private CustomProperties properties;

    private Map<Integer, User> allowedUsersMap;

    private Map<String, String> inputArgs;

    private Bot bot;

    private ObjectHub() {
        properties = new CustomProperties();
        String root = "";
        try {
            properties.load(new FileInputStream(root + "setup.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        archiver = new Archiver(properties);

        executorService = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("threads")));

        initLater();
    }

    private void initLater(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                allowedUsersMap = DBUtil.getAllowedUsersMap();
            }
        });
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

    public Map<String, String> getInputArgs() {
        return inputArgs;
    }

    public void setInputArgs(String[] inputArgs) {
        Map<String, String> argsMap = new HashMap<>();

        for(String s : inputArgs){
            if(s.contains("=")){
                String key = s.substring(0, s.indexOf("="));
                String value = s.substring(s.indexOf("=") + 1, s.length());
                argsMap.put(key.toLowerCase(), value);
            }else{
                System.out.println("Couldnt parse inputArguments, exit.");
                System.exit(0);
            }
        }

        this.inputArgs = argsMap;
    }
}
