package com.backend.http.controller;

import com.StartUp;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;

public abstract class Controller {

    public Logger logger;

    public Controller() {
        logger = StartUp.createLogger(Controller.class);
    }

    public String getLogPrefrix() {
        return LocalDateTime.now().toString() + "\t";
    }


}
