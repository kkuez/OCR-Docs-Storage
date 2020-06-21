package com.gui.controller;

import com.Main;
import org.apache.log4j.Logger;

public abstract class Controller {
     public static final Logger logger = Main.getLogger();

     abstract void closeWindow();

}
