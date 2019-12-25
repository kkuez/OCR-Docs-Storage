package com.controller;

import com.controller.reporter.SubmitBooleanReporter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class BooleanWindow extends Controller{

    @FXML
    Button jaButton;

    @FXML
    Button neinButton;

    @FXML
    Label messageLabel;
    SubmitBooleanReporter reporter;


    public void yes(){
        reporter.submitBoolean(true);
        closeWindow();
    }

    public void no(){
        reporter.submitBoolean(false);
        closeWindow();
    }

    //GETTER SETTER

    public Label getMessageLabel() {
        return messageLabel;
    }

    public void setMessageLabel(Label messageLabel) {
        this.messageLabel = messageLabel;
    }


    public SubmitBooleanReporter getReporter() {
        return reporter;
    }

    public void setReporter(SubmitBooleanReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    void closeWindow() {
        Stage stage = (Stage) jaButton.getScene().getWindow();
        stage.close();
    }
}
