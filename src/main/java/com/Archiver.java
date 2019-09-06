package com;

import com.ObjectTemplates.Document;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Archiver {

    private List<Document> documentList;

    private File chosenFolder;

    public Archiver() {
        documentList = new ArrayList<>();
    }

    public void archive(String nameOfArchive) {
        File tempForZip = new File(chosenFolder.getAbsolutePath(), "temp");
        tempForZip.mkdir();

        documentList.forEach(document -> {
            try {
                FileUtils.copyFile(document.getOriginFile(), new File(tempForZip, document.getOriginalFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        zipDir(tempForZip, nameOfArchive);
        try {
            FileUtils.deleteDirectory(tempForZip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void zipDir(File dir, String nameOfArchive) {
        ZipUtil.pack(dir, new File(chosenFolder, nameOfArchive + ".zip"));
    }

    // GETTER SETTER

    public File getChosenFolder() {
        return chosenFolder;
    }

    public void setChosenFolder(File chosenFolder) {
        this.chosenFolder = chosenFolder;
    }

    public List<Document> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(List<Document> documentList) {
        this.documentList = documentList;
    }
}
