package com.backend;

import com.StartUp;

import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class CustomProperties extends Properties {

    private static Logger logger = StartUp.getLogger();

    @Override
    public synchronized Object setProperty(String key, String value) {
        try {
            this.store(new FileOutputStream("setup.properties"), null);
        } catch (IOException e) {
            logger.error("setup.properties", e);
        }
        return put(key, value);
    }
}
