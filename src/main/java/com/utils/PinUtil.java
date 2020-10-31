package com.utils;

import com.StartUp;
import com.backend.ObjectHub;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Util responsible for physical InputOutput
 */

public class PinUtil {

    private static final int PIN = 25;

    private static boolean isSetup = false;

    private static Logger logger = StartUp.getLogger();

    private PinUtil() {}

    public static void setGPIO(int i, ObjectHub objectHub)  {
        if (isSetup) {
            ProcessBuilder setpinOutPB = new ProcessBuilder();
            setpinOutPB.command("gpio", "write", PIN + "", i + "");
            setpinOutPB.redirectErrorStream();
            setpinOutPB.redirectOutput();
            setpinOutPB.redirectInput();
            try {
                setpinOutPB.start();
            } catch (IOException e) {
                logger.error("Couldnt start GPIO " + i, e);
            }
        } else {
            setupRPIGPIO(objectHub);
        }
    }

    private static void setupRPIGPIO(ObjectHub objectHub){
        if (!objectHub.getProperties().getProperty("debug").equals("true")) {
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
            try {
                setpinOutPB.start();
            } catch (IOException e) {
                logger.error("Couldnt setup GPIO", e);
            }
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
