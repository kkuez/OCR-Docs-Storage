package com.objectTemplates;

import com.backend.OperatingSys;
import com.utils.IOUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.Set;

public class Bon extends Document {

    float sum;

    public Bon(int id, User user, File newPic, float sum) {
        this.setContent("Bon");
        this.setOriginFile(newPic);
        this.setOriginalFileName(newPic.getName());
        this.setTagSet(Set.of());
        this.setId(id);
        this.setUser(user.getId());
        this.setDate(LocalDate.now().toString());
        this.sum = sum;
    }

    public Bon(Document document, float sum) {

    }

    @Override
    public String getInsertDBString(int docCount) {
        if (getDate() == null) {
            setDate(LocalDate.now().toString());
        }
        StringBuilder documentStringBuilder = new StringBuilder("insert into Documents (id, content, originalFile, " +
                "date, user, sizeOfOriginalFile) Values (");

        String originFilePath = getOriginFile().getAbsolutePath();
        originFilePath = IOUtil.convertFilePathOSDependent(originFilePath, OperatingSys.Linux);

        documentStringBuilder.append(getId());
        documentStringBuilder.append(", '");
        documentStringBuilder.append(getContent().replace("'", "''"));
        documentStringBuilder.append("', '");
        documentStringBuilder.append(originFilePath);
        documentStringBuilder.append("', '");
        documentStringBuilder.append(getDate());
        documentStringBuilder.append("', ");
        documentStringBuilder.append(getUser());
        documentStringBuilder.append(",");
        documentStringBuilder.append(FileUtils.sizeOf(getOriginFile()));
        documentStringBuilder.append(");\n");

        StringBuilder bonStatement = new StringBuilder(documentStringBuilder.toString());
        bonStatement.append("insert into Bons (belongsToDocument, sum) Values (");
        bonStatement.append(getId());
        bonStatement.append(", ");
        bonStatement.append(sum);
        bonStatement.append(")");
        return bonStatement.toString();
    }

    // GETTER SETTER

    public float getSum() {
        return sum;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

}
