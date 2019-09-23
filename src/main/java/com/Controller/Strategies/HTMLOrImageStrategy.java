package com.Controller.Strategies;

import com.Controller.Controller;
import com.Controller.SingleDocumentController;
import com.ObjectTemplates.Document;
import com.Utils.LogUtil;
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
            LogUtil.logError(null, e);
        }
        return stage;
    }

    // GETTER SETTER

}
