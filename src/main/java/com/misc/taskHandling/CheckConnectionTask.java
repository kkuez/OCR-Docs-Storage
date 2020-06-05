package com.misc.taskHandling;

import com.Main;
import com.ObjectHub;
import com.telegram.Bot;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.util.Scanner;

public class CheckConnectionTask extends Task {

    private final String GOOGLE_DNS = "1.1.1.1";

    private final String TELEGRAMM_BOTAPI = "https://api.telegram.org";

    private final static int PIN = 25;

    private final Logger logger;

    public CheckConnectionTask(Bot bot) {
        super(bot);
        this.logger = Main.getLogger();
        if (!ObjectHub.getInstance().getProperties().getProperty("debug").equals("true")) {
            try {
                checkWiringPiInstallation();
                logger.info("Wirpingpi found :)");
            } catch (IOException e) {
                logger.info("WiringPi not installed :(");
                logger.info("git clone https://github.com/WiringPi/WiringPi.git");
                logger.info("cd WiringPi");
                logger.info("./build");
                System.exit(2);
            }
            setupRPIGPIO();
        }
    }

    private void setupRPIGPIO() {
        ProcessBuilder setpinOutPB = new ProcessBuilder();
        setpinOutPB.command("gpio", "mode", PIN + "", "out");
        setpinOutPB.redirectErrorStream();
        setpinOutPB.redirectOutput();
        setpinOutPB.redirectInput();
        try {
            setpinOutPB.start();
        } catch (IOException e) {
            logger.info("Couldnt set pin output mode");
            System.exit(2);
        }
    }

    private void checkWiringPiInstallation() throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("gpio");
        pb.redirectErrorStream();
        pb.redirectOutput();
        pb.redirectInput();
        pb.start();
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
        //TODO
        //Return false so it will not be tried to be deleted x) Hack?
        return false;
    }

    public static void setGPIO(int i) throws IOException {
        ProcessBuilder setpinOutPB = new ProcessBuilder();
        setpinOutPB.command("gpio", "write", PIN + "", i + "");
        setpinOutPB.redirectErrorStream();
        setpinOutPB.redirectOutput();
        setpinOutPB.redirectInput();
        setpinOutPB.start();
    }
}
