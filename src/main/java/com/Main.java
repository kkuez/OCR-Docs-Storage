package com;

import com.Controller.StartApplication;
import javafx.application.Application;
import com.Telegram.*;

public class Main {

    public static void main(String[] args) {
        // write your code here
        ObjectHub.getInstance();

        activateTGBot();
        Application.launch(StartApplication.class, args);


        }
    public static void activateTGBot(){
        Bot bot = new Bot();
    }
}
