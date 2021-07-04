package com.backend.taskhandling;

import com.StartUp;
import com.backend.ObjectHub;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;

import static com.utils.PinUtil.setGPIO;

public class CheckConnectionTask extends Task {

    private final String GOOGLE_DNS = "1.1.1.1";


    private final Logger logger;
    private ObjectHub objectHub;

    public CheckConnectionTask(ObjectHub objectHub) {
        this.objectHub = objectHub;
        this.logger = StartUp.getLogger();
    }

    @Override
    public boolean perform() {
        boolean googleUp = false;
        try {
            googleUp = InetAddress.getByName(GOOGLE_DNS).isReachable(1000);
            if (!googleUp) {
                logger.info("Connection problem. Google: " + googleUp);
                setGPIO(1, objectHub);
            } else {
                setGPIO(0, objectHub);
            }
        } catch (IOException e) {
            logger.error("Could not set Pin.", e);
        }
        // TODO
        // Return false so it will not be tried to be deleted x) Hack?
        return false;
    }

}
