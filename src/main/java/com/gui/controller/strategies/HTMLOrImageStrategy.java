package com.gui.controller.strategies;

import com.gui.controller.SingleDocumentController;
import com.objectTemplates.Document;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HTMLOrImageStrategy extends ControllerStrategy {

    private Document document;

    public HTMLOrImageStrategy(Document document) {
        this.document = document;
    }

    @Override
    public Stage getPreparedStage() {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/selectHTMLOrImage.fxml"));
        try {
            Parent root = fxmlLoader.load();
            SingleDocumentController controller = (SingleDocumentController) fxmlLoader.getController();
            controller.setDocument(document);

            stage.setTitle("Wählen der Dateiausgabe.");
            stage.setScene(new Scene(root, 200, 100));
        } catch (IOException e) {
            logger.error(null, e);
        }
        return stage;
    }

    // GETTER SETTER

}