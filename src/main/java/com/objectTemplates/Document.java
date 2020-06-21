package com.objectTemplates;

import com.Main;
import com.backend.DBDAO;
import com.utils.IOUtil;
import com.backend.OperatingSys;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.time.LocalDate;
import java.util.Set;

public abstract class Document{

    private static Logger logger = Main.getLogger();

    private String date;

    private String content;

    private File originFile;

    private String originalFileName;

    private Set<String> tags;

    private int id;

    private int user;

    private String inZipFile;

    public Document() {
    }

    public Document(String content, File originalFile, int user) {
        this.setContent(content);
        this.setOriginFile(originalFile);
        this.user = user;
    }

    public String getInsertDBString(){
        if(date == null){
            date = LocalDate.now().toString();
        }

        String originFilePath = originFile.getAbsolutePath();
        originFilePath = IOUtil.convertFilePathOSDependent(originFilePath, OperatingSys.Linux);

        return "insert into Documents (id, content, originalFile, date, user, sizeOfOriginalFile) Values (" + DBDAO.countDocuments("Documents", "") + ", '" +
                content.replaceAll("'", "''") + "', '" + originFilePath + "', '" + date + "', '" + user + "', " + FileUtils.sizeOf(originFile) + ")";
    }

    // Getter Setter


    public String getInZipFile() {
        return inZipFile;
    }

    public void setInZipFile(String inZipFile) {
        this.inZipFile = inZipFile;
    }


    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }


    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public Set<String> getTagSet() {
        return tags;
    }

    public void setTagSet(Set<String> tags) {
        this.tags = tags;
    }

    public String getOriginalFileName() {
        if (originalFileName == null) {
            originalFileName = originFile.getName();
        }
        return originalFileName;
    }

    public String getTags(){
        StringBuilder stringBuilder = new StringBuilder();
        if(tags != null) {
            tags.forEach(tag -> stringBuilder.append(tag + ", "));
        }else {
            return "";
        }
        return stringBuilder.toString();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public File getOriginFile() {
        return originFile;
    }

    public void setOriginFile(File originFile) {
        if(this.originFile != null){
            DBDAO.executeSQL("update Documents set originalFile='" + originFile.getAbsolutePath() + "' where originalFile='" + this.originFile.getAbsolutePath() + "'");
        }
        this.originFile = originFile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
