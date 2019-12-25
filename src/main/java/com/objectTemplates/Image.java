package com.objectTemplates;

import java.io.File;

public class Image extends Document implements Comparable<Image>{

    private String imageAsBase64 = null;

    public Image(String content, File originalFile, int id) {
        this.setId(id);
        this.setContent(content);
        this.setOriginFile(originalFile);
    }

    public boolean equals(Document document) throws ClassCastException{
        if(document == null){
            throw new NullPointerException();
        }

        Image inputImage = (Image) document;

        return inputImage.getDate().equals(getDate()) && inputImage.getContent().equals(getContent()) && inputImage.getOriginFile().equals(getOriginFile()) && inputImage.getOriginalFileName().equals(getOriginalFileName()) && inputImage.getTags().equals(getTags()) && inputImage.getId() == getId() && inputImage.getUser() == getUser() && inputImage.getInZipFile().equals(getInZipFile());
    }

    // GETTER SETTER

    public String getImageAsBase64() {
        return imageAsBase64;
    }

    public void setImageAsBase64(String imageAsBase64) {
        this.imageAsBase64 = imageAsBase64;
    }

    @Override
    public int compareTo(Image inputImage) {
        if(inputImage == null){
            throw new NullPointerException();
        }
        //TODO methode zuende schreiben±
        return 0;
    }
}
