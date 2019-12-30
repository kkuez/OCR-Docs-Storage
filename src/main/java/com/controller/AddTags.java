package com.controller;

import com.Main;
import com.controller.reporter.*;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class AddTags extends SimpleSubmitController{
    private static Logger logger = Main.getLogger();

    @FXML
    TextField newTagTextField;

    @FXML
    Label newTagLabel;

    @FXML
    Button doneButton;

    @FXML
    Button addButton;

    Set<String> tags;
    Reporter reporter;

    public AddTags(){
        tags = new HashSet<>();
    }

    public void addTag(){
        tags.add(newTagTextField.getText());
        StringBuilder labelText = new StringBuilder();
        tags.forEach(tag -> {
            labelText.append(tag + ", ");
        });
        newTagLabel.setText(labelText.toString());
        newTagTextField.setText("");
    }

    @Override
    public void submit() {
        logger.info("Gui: " + "Added Tags.");
        ((SubmitTagsReporter) reporter).submitTags(tags);
        closeWindow();
    }

    //GETTER SETTER
    public Reporter getReporter() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }


    @Override
    void closeWindow() {
        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }
}
