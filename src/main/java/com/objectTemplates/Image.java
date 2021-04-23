package com.objectTemplates;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class Image extends Document implements Comparable<Image>{

    DateTimeFormatter germanFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMAN);

    public Image(String content, File originalFile, int id, String user) {
        setId(id);
        setContent(content);
        setOriginFile(originalFile);
        String date = LocalDate.now().format(germanFormatter);
        setDate(date);
        setUser(user);
        setOriginalFileName(originalFile.getName());
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
