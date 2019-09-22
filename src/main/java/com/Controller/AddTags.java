package com.Controller;

import com.Controller.Reporter.*;
import com.Utils.LogUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class AddTags extends SimpleSubmitController{

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
        LogUtil.log("Gui: " + "Added Tags.");
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
