package com.objectTemplates;

import java.io.File;
import java.time.LocalDate;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.backend.OperatingSys;
import com.utils.IOUtil;

public abstract class Document {

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

    public String getInsertDBString(int docCount) {
        if (date == null) {
            date = LocalDate.now().toString();
        }

        String originFilePath = originFile.getAbsolutePath();
        originFilePath = IOUtil.convertFilePathOSDependent(originFilePath, OperatingSys.Linux);

        return "insert into Documents (id, content, originalFile, date, user, sizeOfOriginalFile) Values (" + docCount
                + ", '" + content.replace("'", "''") + "', '" + originFilePath + "', '" + date + "', " + user + ", "
                + FileUtils.sizeOf(originFile) + ")";
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

    public String getTags() {
        StringBuilder stringBuilder = new StringBuilder();
        if (tags != null) {
            tags.forEach(tag -> stringBuilder.append(tag + ", "));
        } else {
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
        this.originFile = originFile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
