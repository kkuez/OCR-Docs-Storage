package com.backend;

import com.data.Document;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class Archiver {

    private List<Document> documentList;

    File archiveFolder;

    File documentFolder;

    File bonFolder;

    File zipFolder;

    final File resourceFolder;

    public Archiver(CustomProperties properties) {

        documentList = new ArrayList<>();
        archiveFolder = new File(properties.getProperty("pathToProjectFolder") + File.separator + "Archiv", LocalDate.now().getMonth().toString() + "_" + LocalDate.now().getYear());
        if(!archiveFolder.exists()){
            archiveFolder.mkdir();
        }

        zipFolder = new File(archiveFolder.getParent(), "Zips");
        if(!zipFolder.exists()){
            zipFolder.mkdir();
        }

        documentFolder = new File(archiveFolder, "Documents");
        if(!documentFolder.exists()){
            documentFolder.mkdir();
        }

        bonFolder = new File(archiveFolder, "Bons");
        if(!bonFolder.exists()){
            bonFolder.mkdir();
        }

        resourceFolder = new File(archiveFolder, "resources");
        if(!resourceFolder.exists()){
            resourceFolder.mkdir();
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
