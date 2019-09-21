package com.Controller.Strategies;

import com.Controller.BooleanWindow;
import com.Controller.Reporter.Reporter;
import com.Controller.Reporter.SubmitBooleanReporter;
import com.Controller.SingleDocumentController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BooleanWIndowStrategy extends ControllerStrategy {

    Reporter reporter;

    public BooleanWIndowStrategy(Reporter reporter){
        this.reporter = reporter;
    }


    @Override
    public Stage getPreparedStage() {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/BooleanWindow.fxml"));
        try {
            Parent root = fxmlLoader.load();
            BooleanWindow controller = (BooleanWindow) fxmlLoader.getController();
            controller.setReporter((SubmitBooleanReporter) reporter);
            controller.getMessageLabel().setText("Sollen Tags erstellt werden?");
            stage.setTitle("Sollen Tags erstellt werden?");
            stage.setScene(new Scene(root, 400, 300));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stage;
    }
}
