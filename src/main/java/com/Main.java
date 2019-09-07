package com;

import com.Controller.StartApplication;
import javafx.application.Application;

public class Main {

    public static void main(String[] args) {
        // write your code here
        ObjectHub.getInstance();
        Application.launch(StartApplication.class, args);

    }
}
