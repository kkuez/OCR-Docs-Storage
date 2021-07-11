package com.backend.http.controller;

import com.backend.CustomProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@RestController
public class LogController extends Controller {

    @ResponseBody
    @RequestMapping("log/getLog")
    public ResponseEntity<String> getLog(HttpServletRequest request) {
        File monthFolder = new File(new CustomProperties().getProperty("localArchivePath"),
                LocalDate.now().getMonth().toString() + "_" + LocalDate.now().getYear());
        File logFolder = new File(monthFolder, "Logs");
        final List<File> logFiles = new ArrayList();
        logFiles.addAll(Arrays.asList(logFolder.listFiles()));
        logFiles.sort((o1, o2) -> {
            File log1 = o1;
            File log2 = o2;
            if(log1.lastModified() > log2.lastModified()) {
                return 1;
            } else if (log1.lastModified() < log2.lastModified()) {
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
            logger.error("Could not read logFile", e);
        }
        return ResponseEntity.ok(builder.toString());
    }
}
