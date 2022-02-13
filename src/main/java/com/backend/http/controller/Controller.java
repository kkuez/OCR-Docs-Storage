package com.backend.http.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public abstract class Controller {

    public final Logger logger;

    public Controller() {
        logger = LoggerFactory.getLogger(Controller.class);
    }

    public String getLogPrefrix() {
        return LocalDateTime.now() + "\t";
    }


}
