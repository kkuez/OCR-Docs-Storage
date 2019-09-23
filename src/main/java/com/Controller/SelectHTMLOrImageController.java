package com.Controller;

import com.ObjectTemplates.Document;
import com.Utils.LogUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SelectHTMLOrImageController extends SingleDocumentController {

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
            closeWindow();
        } catch (IOException e) {
            LogUtil.logError(null, e);
        }
    }

    public void showImage() {
        try {
            Desktop.getDesktop().open(document.getOriginFile());
            closeWindow();
        } catch (IOException e) {
            LogUtil.logError(null, e);
        }
    }

    // GETTER SETTER

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    void closeWindow() {
        Stage stage = (Stage) bildButton.getScene().getWindow();
        stage.close();
    }
}
