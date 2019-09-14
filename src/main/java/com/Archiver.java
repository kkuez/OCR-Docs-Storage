package com;

import com.ObjectTemplates.Document;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Archiver {

    private List<Document> documentList;

    File archiveFolder;

    File documentFolder;

    File bonFolder;

    File zipFolder;

    public Archiver(Properties properties) {
        documentList = new ArrayList<>();
        archiveFolder = new File(properties.getProperty("localArchivePath"), LocalDate.now().getMonth().toString() + "_" + LocalDate.now().getYear());
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
    }

    public void archive(String nameOfArchive) {
        File tempForZip = new File(ObjectHub.getInstance().getProperties().getProperty("localArchivePath"), "temp");
        tempForZip.mkdir();

        documentList.forEach(document -> {
            try {
                FileUtils.copyFile(document.getOriginFile(), new File(tempForZip, document.getOriginalFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        zipDir(tempForZip, nameOfArchive);
        File zippedDir = new File(ObjectHub.getInstance().getProperties().getProperty("localArchivePath"), nameOfArchive + ".zip");
        try {
            FileUtils.copyFile(zippedDir, new File(ObjectHub.getInstance().getArchiver().getZipFolder(), nameOfArchive + ".zip"));
            FileUtils.deleteDirectory(tempForZip);
            FileUtils.deleteQuietly(zippedDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void zipDir(File dir, String nameOfArchive) {
        ZipUtil.pack(dir, new File(ObjectHub.getInstance().getProperties().getProperty("localArchivePath"), nameOfArchive + ".zip"));
    }

    // GETTER SETTER


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
}
