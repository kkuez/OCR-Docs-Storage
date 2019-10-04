package com.ObjectTemplates;

import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Telegram.Processes.*;
import com.Telegram.Processes.Process;
import com.Utils.BotUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class User {
    private int id;

    private String name;

    private Process process = null;

    boolean aboutToUploadFile = false;

    public void deleteProcessEventually(Bot bot, Update update){
        if(process != null && process.getDeleteLater()){
            process = null;
        }
    };

    //GETTER SETTER

    public boolean isAboutToUploadFile() {
        return aboutToUploadFile;
    }

    public void setAboutToUploadFile(boolean aboutToUploadFile) {
        this.aboutToUploadFile = aboutToUploadFile;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User(int id, String name){
        this.id = id;
        this.name = name;
    }

}
