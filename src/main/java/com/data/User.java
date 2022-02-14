package com.data;

public class User {


    private String name;

    private Process process = null;

    private boolean isBusy = false;

    public User(String name) {
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
