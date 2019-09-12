package com.ObjectTemplates;

import com.Utils.TessUtil;

import java.io.File;

public class Bon extends Document {

    double sum;

    public Bon(String content, File originalFile) {
        this.setContent(content);
        this.setOriginFile(originalFile);
        this.setTags("");
        sum = TessUtil.getLastNumber(content);
    }


}
