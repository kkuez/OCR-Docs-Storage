package com.backend.http.controller;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.time.LocalDateTime;

public abstract class Controller {

    public Logger logger;

    public Controller() {
        logger = Logger.getLogger(Controller.class);
        PropertyConfigurator.configure(getClass().getResourceAsStream("/log4j.properties"));
        logger.addAppender(new ConsoleAppender());
    }

    public String getLogPrefrix() {
        return LocalDateTime.now().toString() + "\t";
    }
}
