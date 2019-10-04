package com.ObjectTemplates;

import com.Telegram.Processes.*;
import com.Telegram.Processes.Process;

public class User {
    private int id;

    private String name;

    private Process process = null;

    public void deleteProcessEventually(){
        if(process.getDeleteLater()){
            process = null;
        }
    };

    //GETTER SETTER

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
