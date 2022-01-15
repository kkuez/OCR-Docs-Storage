package com.backend.taskhandling;

import com.backend.ObjectHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;

import static com.utils.PinUtil.setGPIO;

public class CheckConnectionTask extends Task {

    private final String GOOGLE_DNS = "1.1.1.1";


    private final Logger logger;
    private ObjectHub objectHub;

    public CheckConnectionTask(ObjectHub objectHub) {
        this.objectHub = objectHub;
        this.logger = LoggerFactory.getLogger(CheckConnectionTask.class);
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

    @Override
    public boolean timeIsNow(LocalDateTime localDateTime) {
        return true;
    }

    @Override
    public void delete() {
    }
}
