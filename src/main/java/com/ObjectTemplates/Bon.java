package com.ObjectTemplates;

import com.Utils.DBUtil;
import com.Utils.TessUtil;

import java.io.File;

public class Bon extends Document {

    public float getSum() {
        return sum;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

    float sum;

    int belongsToDocument;

    public Bon(String content, File originalFile, float sum, int belongsToDocument) {
        this.setContent(content);
        this.setOriginFile(originalFile);
        this.setTags("");
        this.sum = sum;
        this.belongsToDocument = belongsToDocument;
    }

    @Override
    public String getInsertDBString(){
        return "insert into Bons (belongsToDocument, sum) Values (" + belongsToDocument + ", " + sum + ")";
    }
}
