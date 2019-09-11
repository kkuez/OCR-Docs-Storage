package com.Misc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class CustomProperties extends Properties {
    @Override
    public synchronized Object setProperty(String key, String value) {
        try {
            this.store(new FileOutputStream("setup.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return put(key, value);
    }
}
