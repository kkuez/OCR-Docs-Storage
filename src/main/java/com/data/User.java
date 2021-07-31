package com.data;

import com.backend.BackendFacade;

public class User {

    private BackendFacade facade = null;

    private String name;

    private Process process = null;

    private boolean isBusy = false;

    // NO InlineKeyboards! important to seperat input of the to List processes.

    public User(String name, BackendFacade facade) {
        this.facade = facade;
        this.name = name;
    }

    // GETTER SETTER

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
