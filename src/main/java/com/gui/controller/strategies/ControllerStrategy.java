package com.gui.controller.strategies;

import com.Main;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

public abstract class ControllerStrategy{

    // GETTER SETTER
    public static final Logger logger = Main.getLogger();

    public abstract Stage getPreparedStage();

}
