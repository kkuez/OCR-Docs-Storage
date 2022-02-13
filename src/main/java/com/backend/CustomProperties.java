package com.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Service
public class CustomProperties extends Properties {

    private static final Logger logger = LoggerFactory.getLogger(CustomProperties.class);

    @Autowired
    public CustomProperties() {
        String root = "";
        try {
            this.load(new FileInputStream(root + "setup.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

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
