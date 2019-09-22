package com;

import com.Controller.MainController;
import com.Misc.CustomProperties;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
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

    private Map<Update, Bot> performUpdateLaterMap;

    private Thread performUpdateLaterThread;

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
                performUpdateLaterMap = new HashMap<>();
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

    public Thread getPerformUpdateLaterThread() {
        return performUpdateLaterThread;
    }

    public void setPerformUpdateLaterThread(Thread performUpdateLaterThread) {
        this.performUpdateLaterThread = performUpdateLaterThread;
    }

    public Map<Update, Bot> getPerformUpdateLaterMap() {
        if(performUpdateLaterThread == null){
            performUpdateLaterThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(performUpdateLaterMap.size() != 0) {
                        try {
                            int sleepTimeInMs = 600000;
                            //Wait 10 minutes
                            LogUtil.log("System: Waiting to perform LaterUpdates... (" + performUpdateLaterMap.size() + ", " + sleepTimeInMs / 1000 + " seconds)");
                            Thread.sleep(sleepTimeInMs);
                            performUpdateLaterMap.keySet().forEach(update -> {
                                Bot bot = performUpdateLaterMap.get(update);

                                try {
                                    LogUtil.log("System: Trying to perform LaterUpdate");
                                    bot.processUpdateReceveived(update);
                                    performUpdateLaterMap.remove(update);
                                } catch (Exception e) {
                                    BotUtil.activateTGBot(bot);
                                    e.printStackTrace();
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    LogUtil.log("System: No LaterUpdates left.");
                    performUpdateLaterMap = null;
                    performUpdateLaterThread = null;
                }
            });
            performUpdateLaterThread.start();
        }
        return performUpdateLaterMap;
    }

    public void setPerformUpdateLaterMap(Map<Update, Bot> performUpdateLaterMap) {
        this.performUpdateLaterMap = performUpdateLaterMap;
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
            }
        }

        this.inputArgs = argsMap;
    }
}
