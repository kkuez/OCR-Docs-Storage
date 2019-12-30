package com.objectTemplates;

import com.Main;
import org.apache.log4j.Logger;

import java.io.File;

public class Project {

    private static Logger logger = Main.logger;


    private File location;

    private String name;

    // GETTER SETTER
    public File getLocation() {
        return location;
    }

    public void setLocation(File location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
