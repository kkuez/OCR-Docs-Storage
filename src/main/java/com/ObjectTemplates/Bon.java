package com.ObjectTemplates;

import java.io.File;
import java.util.Collections;

public class Bon extends Document {

    public float getSum() {
        return sum;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

    float sum;

    int belongsToDocument;

    public Bon(int belongsToDocument, float sum) {
        this.setTagSet(Collections.EMPTY_SET);
        this.sum = sum;
        this.belongsToDocument = belongsToDocument;
    }

    public Bon(String content, File originalFile, float sum, int belongsToDocument) {
        this.setContent(content);
        this.setOriginFile(originalFile);
        this.setTagSet(Collections.EMPTY_SET);
        this.sum = sum;
        this.belongsToDocument = belongsToDocument;
    }

    @Override
    public String getInsertDBString(){
        return "insert into Bons (belongsToDocument, sum) Values (" + belongsToDocument + ", " + sum + ")";
    }

    //GETTER SETTER

    public int getBelongsToDocument() {
        return belongsToDocument;
    }

    public void setBelongsToDocument(int belongsToDocument) {
        this.belongsToDocument = belongsToDocument;
    }

}
