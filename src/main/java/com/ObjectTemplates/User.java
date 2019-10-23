package com.ObjectTemplates;

import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Telegram.Processes.Process;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public class User {
    private int id;

    private String name;

    private Process process = null;

    boolean aboutToUploadFile = false;

    private boolean isBusy = false;
    //NO InlineKeyboards! important to seperat input of the to List processes.
    private ReplyKeyboard keyboardContext = KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Start, false, false, "");

    public void deleteProcessEventually(Bot bot, Update update){
        if(process != null && process.getDeleteLater()){
            process = null;
        }
    };

    //GETTER SETTER

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public ReplyKeyboard getKeyboardContext() {
        return keyboardContext;
    }

    public void setKeyboardContext(ReplyKeyboard keyboardContext) {
        this.keyboardContext = keyboardContext;
    }

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
