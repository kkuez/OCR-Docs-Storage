package com.backend.http.controller;

import com.backend.CustomProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@RestController
public class LogController extends Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogController.class);

    @ResponseBody
    @RequestMapping("log/getLog")
    public ResponseEntity<String> getLog(HttpServletRequest request) {
        final File logFolder = getCurrentLogFolder();
        final File[] files = logFolder.listFiles();
        if (files == null) {
            LOGGER.error("Could not find logs in {}", logFolder.getAbsolutePath());
            return ResponseEntity.ok("Could not find logs");
        }

        final List<File> logFiles = new ArrayList(Arrays.asList(files));
        logFiles.sort((o1, o2) -> {
            long log1LastMod = o1.lastModified();
            long log2LastMod = o2.lastModified();
            if(log1LastMod > log2LastMod) {
                return 1;
            } else if (log1LastMod < log2LastMod) {
                return -1;
            }
            return 0;
        });

        final File lastLog = logFiles.get(logFiles.size() - 1);
        StringBuilder builder = new StringBuilder();
        try(Scanner scanner = new Scanner(new FileInputStream(lastLog))) {
            while(scanner.hasNextLine())
            {
                builder.append(scanner.nextLine()).append("\n");
            }
        } catch (IOException e) {
            LOGGER.error("Could not read logFile", e);
        }
        return ResponseEntity.ok(builder.toString());
    }

    private File getCurrentLogFolder() {
        File logFolder = new File(new CustomProperties().getProperty("localArchivePath"), ".log");
        File yearMonthLogFolder;
        LocalDate currentDate = LocalDate.now();

        // To prevent infinite loops stop when certain index is reached
        int errorIndex = 0;
        do {
            Integer monthInt = currentDate.getMonthValue();
            String monthVal = (monthInt + "").length() == 1 ? 0 + "" + monthInt : monthInt + "";
            yearMonthLogFolder = new File(logFolder, currentDate.getYear() + "-" + monthVal);
            currentDate = currentDate.minus(1, ChronoUnit.MONTHS);
            errorIndex++;
            if(errorIndex > 99) {
                throw new RuntimeException("Could not find any logfolder!");
            }
        } while (!yearMonthLogFolder.exists());

        return yearMonthLogFolder;
    }
}
