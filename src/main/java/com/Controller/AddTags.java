package com.Controller;

import com.Controller.Actions.TagAction;
import com.Controller.Reporter.Reporter;
import com.Controller.Reporter.SubmitTagsReporter;
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

        ((SubmitTagsReporter) reporter).submitTags(tags);
        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }

    //GETTER SETTER


    public Reporter getReporter() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }


}
