package com.controller.strategies;

import com.Main;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

public abstract class ControllerStrategy{

    // GETTER SETTER
    public static Logger logger = Main.logger;

    public abstract Stage getPreparedStage();

}
