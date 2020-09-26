package com.backend.taskhandling;

import static com.utils.PinUtil.setGPIO;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.Main;
import com.bot.telegram.Bot;

public class CheckConnectionTask extends Task {

    private final String GOOGLE_DNS = "1.1.1.1";

    private final String TELEGRAMM_BOTAPI = "https://api.telegram.org";

    private final Logger logger;

    public CheckConnectionTask(Bot bot) {
        super(bot);
        this.logger = Main.getLogger();
    }

    @Override
    public boolean perform() {
        boolean googleUp = false;
        try {
            googleUp = InetAddress.getByName(GOOGLE_DNS).isReachable(1000);
            if (!googleUp) {
                logger.info("Connection problem. Google: " + googleUp);
                setGPIO(1);
            } else {
                setGPIO(0);
            }
        } catch (IOException e) {
            logger.error("Could not set Pin.", e);
        }
        // TODO
        // Return false so it will not be tried to be deleted x) Hack?
        return false;
    }

}
