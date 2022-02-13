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

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomProperties.class);
    private static final String SETUPPROPERTIES_NAME = "setup.properties";

    @Autowired
    public CustomProperties() {
        String root = "";
        try {

            this.load(new FileInputStream(root + SETUPPROPERTIES_NAME));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        try(final FileOutputStream fos = new FileOutputStream(SETUPPROPERTIES_NAME)) {
            this.store(fos, null);
        } catch (IOException e) {
            LOGGER.error(SETUPPROPERTIES_NAME, e);
        }
        return put(key, value);
    }
}
