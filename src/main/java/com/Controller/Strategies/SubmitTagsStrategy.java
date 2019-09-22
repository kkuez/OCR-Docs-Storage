package com.Controller.Strategies;

import com.Controller.AddTags;
import com.Controller.BooleanWindow;
import com.Controller.Reporter.Reporter;
import com.Controller.Reporter.SubmitBooleanReporter;
import com.Controller.Reporter.SubmitTagsReporter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SubmitTagsStrategy extends ControllerStrategy {
    Reporter reporter;

    public SubmitTagsStrategy(Reporter reporter){
        this.reporter = reporter;
    }

    @Override
    public Stage getPreparedStage() {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AddTags.fxml"));
        try {
            Parent root = fxmlLoader.load();
            AddTags controller = (AddTags) fxmlLoader.getController();
            controller.setReporter((SubmitTagsReporter) reporter);
            stage.setTitle("Tags erstellen");
            stage.setScene(new Scene(root, 400, 300));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stage;
    }
}