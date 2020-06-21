package com.objectTemplates;

import com.backend.DBDAO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collections;

public class Bon extends Document {

    public Bon(Document document, float sum) {
        this.setContent(document.getContent());
        this.setOriginFile(document.getOriginFile());
        this.setTagSet(document.getTagSet());
        this.setId(document.getId());
        this.setUser(document.getUser());
        this.setDate(document.getDate());
        this.sum = sum;

    }

    @Override
    public String getInsertDBString(int docCount){
        String divider = ", '";
        StringBuilder docStatement = new StringBuilder("insert into Documents (id, content, originalFile, date, user, sizeOfOriginalFile) Values (");
        docStatement.append(docCount);
        docStatement.append(divider);
        docStatement.append(getContent().replaceAll("'", "''"));
        docStatement.append(divider);
        docStatement.append(getOriginFile().getAbsolutePath());
        docStatement.append(divider);
        docStatement.append(getDate());
        docStatement.append(divider);
        docStatement.append(getUser());
        docStatement.append(", ");
        docStatement.append(FileUtils.sizeOf(getOriginFile()));
        docStatement.append(");");

        StringBuilder bonStatement = new StringBuilder("insert into Bons (belongsToDocument, sum) Values (");
        bonStatement.append(docCount);
        bonStatement.append(", ");
        bonStatement.append(sum);
        bonStatement.append(")");
        return docStatement.toString() + ";" + bonStatement.toString();
    }


    //GETTER SETTER
    public float getSum() {
        return sum;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

    float sum;

}
