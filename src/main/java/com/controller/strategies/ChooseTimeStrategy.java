package com.controller.strategies;

import com.controller.ChooseTimeController;
import com.controller.reporter.ChooseTimeReporter;
import com.controller.reporter.Reporter;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChooseTimeStrategy extends ControllerStrategy {

    private ChooseTimeReporter reporter;

    public ChooseTimeStrategy(Reporter reporter){
        this.reporter = (ChooseTimeReporter) reporter;
    }

    @Override
    public Stage getPreparedStage() {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/chooseTime.fxml"));
        try {
            Parent root = fxmlLoader.load();
            ChooseTimeController controller = (ChooseTimeController) fxmlLoader.getController();
            controller.setReporter(reporter);
            stage.setTitle("PDF erstellen=> Zeitpunkt wählen");
            stage.setScene(new Scene(root, 600, 350));
        } catch (IOException e) {
            logger.error(null, e);
        }
        return stage;
    }
}
