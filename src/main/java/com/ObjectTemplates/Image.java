package com.ObjectTemplates;

import java.io.File;

public class Image extends Document {

    private String imageAsBase64 = null;

    public Image(String content, File originalFile, int id) {
        this.setId(id);
        this.setContent(content);
        this.setOriginFile(originalFile);
    }

    // GETTER SETTER

    public String getImageAsBase64() {
        return imageAsBase64;
    }

    public void setImageAsBase64(String imageAsBase64) {
        this.imageAsBase64 = imageAsBase64;
    }

}
