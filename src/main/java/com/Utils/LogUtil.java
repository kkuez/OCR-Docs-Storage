package com.Utils;

import com.ObjectHub;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LogUtil {

    public static void log(String message){
        String logString = LocalDateTime.now().toString().replace("T", " ") + "      " + message;
        checkLogFile(logString);
        try {
            FileUtils.writeStringToFile(ObjectHub.getInstance().getArchiver().getCurrentLogFile(), logString, UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(logString);
    }



    public static void checkLogFile(String logString){
        if(ObjectHub.getInstance().getArchiver().getCurrentLogFile() == null){
            File logFile = new File(ObjectHub.getInstance().getArchiver().getLogFolder(), LocalDateTime.now().toString().replace(".", "-").replace(":", "_") + ".log");
            ObjectHub.getInstance().getArchiver().setCurrentLogFile(logFile);
            try {
                FileUtils.write(ObjectHub.getInstance().getArchiver().getCurrentLogFile(), logString, UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
