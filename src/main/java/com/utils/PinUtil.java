package com.utils;

import com.Main;
import com.backend.ObjectHub;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Util responsible for physical InputOutput
 */

public class PinUtil {

    private final static int PIN = 25;

    private static boolean isSetup = false;

    private static Logger logger = Main.getLogger();

    public static void setGPIO(int i) throws IOException {
        if (isSetup) {
            ProcessBuilder setpinOutPB = new ProcessBuilder();
            setpinOutPB.command("gpio", "write", PIN + "", i + "");
            setpinOutPB.redirectErrorStream();
            setpinOutPB.redirectOutput();
            setpinOutPB.redirectInput();
            setpinOutPB.start();
        } else {
            setupRPIGPIO();
        }
    }

    private static void setupRPIGPIO() throws IOException {
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
            ProcessBuilder setpinOutPB = new ProcessBuilder();
            setpinOutPB.command("gpio", "mode", PIN + "", "out");
            setpinOutPB.redirectErrorStream();
            setpinOutPB.redirectOutput();
            setpinOutPB.redirectInput();
            setpinOutPB.start();
            isSetup = true;
        }
    }

    private static void checkWiringPiInstallation() throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("gpio");
        pb.redirectErrorStream();
        pb.redirectOutput();
        pb.redirectInput();
        pb.start();
    }

}
