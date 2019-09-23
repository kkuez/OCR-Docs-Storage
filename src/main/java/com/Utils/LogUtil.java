package com.Utils;

import com.ObjectHub;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LogUtil {

    public static void log(String message){
        String logString = LocalDateTime.now().toString().replace("T", " ") + "      " + message + "\n";
        checkLogFile();
        try {
            FileUtils.write(ObjectHub.getInstance().getArchiver().getCurrentLogFile(), logString, UTF_8, true);
        } catch (IOException e) {
            LogUtil.logError(ObjectHub.getInstance().getArchiver().getCurrentLogFile().getAbsolutePath(), e);
        }
        System.out.print(logString);
    }

    public static void logError(String messageOrNull, Exception e){
        String message = messageOrNull == null ? "" : messageOrNull;
        StringBuilder errorSB = new StringBuilder(LocalDateTime.now().toString().replace("T", " ") + "      =========== ERROR =================\n");
        errorSB.append(LocalDateTime.now().toString().replace("T", " ") + "      " + message + "\n");
        String stacktrace = ExceptionUtils.getStackTrace(e);
        errorSB.append(stacktrace + "\n");
        checkLogFile();
        try {
            FileUtils.write(ObjectHub.getInstance().getArchiver().getCurrentLogFile(), errorSB.toString(), UTF_8, true);
        } catch (IOException ex) {
            e.printStackTrace();
        }
        System.out.print(errorSB.toString());
    }

    public static void checkLogFile(){
        if(ObjectHub.getInstance().getArchiver().getCurrentLogFile() == null){
            File logFile = new File(ObjectHub.getInstance().getArchiver().getLogFolder(), LocalDateTime.now().toString().replace(".", "-").replace(":", "_") + ".log");
            ObjectHub.getInstance().getArchiver().setCurrentLogFile(logFile);
            try {
                FileUtils.write(ObjectHub.getInstance().getArchiver().getCurrentLogFile(), "Start.", UTF_8);
                System.out.println("Logfile: " + logFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
