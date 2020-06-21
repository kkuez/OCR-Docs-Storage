package com.objectTemplates;

import java.io.File;

public class Image extends Document implements Comparable<Image>{

    public Image(String content, File originalFile, int id) {
        this.setId(id);
        this.setContent(content);
        this.setOriginFile(originalFile);
    }

    @Override
    public boolean equals(Object obj) throws ClassCastException{
        if(obj == null){
            throw new NullPointerException();
        }
        if(!obj.getClass().equals(Document.class)){
            return false;
        }
        Document document = (Document) obj;


        Image inputImage = (Image) document;

        return inputImage.getDate().equals(getDate()) && inputImage.getContent().equals(getContent()) && inputImage.getOriginFile().equals(getOriginFile()) && inputImage.getOriginalFileName().equals(getOriginalFileName()) && inputImage.getTags().equals(getTags()) && inputImage.getId() == getId() && inputImage.getUser() == getUser() && inputImage.getInZipFile().equals(getInZipFile());
    }

    @Override
    public int hashCode(){
        return toString().hashCode();
    }

    // GETTER SETTER

    @Override
    public int compareTo(Image inputImage) {
        if(inputImage == null){
            throw new NullPointerException();
        }
        //TODO methode zuende schreiben±
        return 0;
    }
}
