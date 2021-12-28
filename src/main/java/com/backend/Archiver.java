package com.backend;

import com.data.Document;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class Archiver {

    private List<Document> documentList;

    private static Logger logger = LoggerFactory.getLogger(Archiver.class);

    File archiveFolder;

    File documentFolder;

    File bonFolder;

    File zipFolder;

    File currentLogFile;

    File resourceFolder;

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

    public File getResourceFolder() {
        return resourceFolder;
    }

    public File getCurrentLogFile() {
        return currentLogFile;
    }

    public void setCurrentLogFile(File currentLogFile) {
        this.currentLogFile = currentLogFile;
    }

    public File getZipFolder() {
        return zipFolder;
    }

    public void setZipFolder(File zipFolder) {
        this.zipFolder = zipFolder;
    }

    public File getArchiveFolder() {
        return archiveFolder;
    }

    public void setArchiveFolder(File archiveFolder) {
        this.archiveFolder = archiveFolder;
    }

    public File getDocumentFolder() {
        return documentFolder;
    }

    public void setDocumentFolder(File documentFolder) {
        this.documentFolder = documentFolder;
    }

    public File getBonFolder() {
        return bonFolder;
    }

    public void setBonFolder(File bonFolder) {
        this.bonFolder = bonFolder;
    }


    public List<Document> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(List<Document> documentList) {
        this.documentList = documentList;
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
