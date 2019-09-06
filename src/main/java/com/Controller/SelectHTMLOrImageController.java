package com.Controller;

import com.ObjectTemplates.Document;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SelectHTMLOrImageController extends Controller {

    @FXML
    Button hTMLButton;

    @FXML
    Button bildButton;

    Document document;

    public void showHTML() {
        try {
            File tempFile = File.createTempFile(document.getOriginalFileName(), ".html");
            FileUtils.writeStringToFile(tempFile, document.getContent(), "UTF-8");
            tempFile.deleteOnExit();
            Desktop.getDesktop().open(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showImage() {
        try {
            Desktop.getDesktop().open(document.getOriginFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // GETTER SETTER

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

}
