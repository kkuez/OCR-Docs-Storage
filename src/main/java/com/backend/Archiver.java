package com.backend;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class Archiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Archiver.class);

    private final File bonFolder;

    public Archiver(CustomProperties properties) {
        File archiveFolder = new File(properties.getProperty("pathToProjectFolder") + File.separator + "Archiv",
                LocalDate.now().getMonth().toString() + "_" + LocalDate.now().getYear());
       boolean exit = false;
        if(!archiveFolder.exists()){
            exit = !archiveFolder.mkdir();
        }
        if(exit) {
            LOGGER.error("Could not create dir {}", archiveFolder.getAbsolutePath());
            System.exit(6);
        }

        bonFolder = new File(archiveFolder, "Bons");
        if(!bonFolder.exists()){
            exit = !bonFolder.mkdir();
        }
        if(exit) {
            LOGGER.error("Could not create dir {}", bonFolder.getAbsolutePath());
            System.exit(7);
        }
    }

    public File copyToArchive(File newPic, boolean isBon) {
        if(isBon) {
            File copiedFile = new File(bonFolder, newPic.getName());
            try {
                FileUtils.copyFile(newPic, copiedFile);
                return copiedFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
