package com.objectTemplates;

import com.backend.BackendFacade;
import com.bot.telegram.KeyboardFactory;
import com.bot.telegram.processes.Process;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class User {

    private BackendFacade facade = null;

    private int id;

    private String name;

    private Process process = null;

    private boolean isBusy = false;
    //NO InlineKeyboards! important to seperat input of the to List processes.
    private ReplyKeyboard keyboardContext = KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Start, false, false, "", facade);

    public User(int id, String name, BackendFacade facade){
        this.facade = facade;
        this.id = id;
        this.name = name;
    }

    public void deleteProcessEventually(){
        if(process != null && process.getDeleteLater()){
            process = null;
        }
    }

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

}
