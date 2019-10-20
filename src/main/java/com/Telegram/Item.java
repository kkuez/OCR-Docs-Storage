package com.Telegram;

import java.io.File;

public class Item {

    String name;

    File picturePath;

    public Item(String name, File picturePath){
        this.name = name;
        this.picturePath = picturePath;
    }

    //GETTER SETTER

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(File picturePath) {
        this.picturePath = picturePath;
    }
}
