package com.ObjectTemplates;

import java.io.File;

public abstract class Document {

    private String date;

    private String content;

    private File originFile;

    private String originalFileName;

    private String tags;

    private int id;

    public Document() {
    }

    public Document(String content, File originalFile) {
        this.setContent(content);
        this.setOriginFile(originalFile);
        this.tags = "";
    }

    // Getter Setter



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
        this.originFile = originFile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
