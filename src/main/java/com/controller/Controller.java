package com.controller;

import com.Main;
import org.apache.log4j.Logger;

public abstract class Controller {
     public static Logger logger = Main.logger;

     abstract void closeWindow();
}
