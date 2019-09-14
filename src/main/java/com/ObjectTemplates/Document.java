package com.ObjectTemplates;

import com.Utils.DBUtil;

import java.io.File;

public abstract class Document{

    private String date;

    private String content;

    private File originFile;

    private String originalFileName;

    private String tags;

    private int id;

    private int user;

    private String inZipFile;

    public Document() {
    }

    public Document(String content, File originalFile, int user) {
        this.setContent(content);
        this.setOriginFile(originalFile);
        this.user = user;
        this.tags = "";
    }

    public String getInsertDBString(){
        return "insert into Documents (id, content, originalFile, date, user) Values (" + DBUtil.countDocuments() + ", '" +
                content.replaceAll("'", "''") + "', '" + originFile.getAbsolutePath() + "', '" + date + "', '" + user + "')";
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getOriginalFileName() {
        if (originalFileName == null) {
            originalFileName = originFile.getName();
        }
        return originalFileName;
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
            DBUtil.executeSQL("update Documents set originalFile='" + originFile.getAbsolutePath() + "' where originalFile='" + this.originFile.getAbsolutePath() + "'");
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
