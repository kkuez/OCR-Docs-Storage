package com.Controller;

import com.Controller.Reporter.Reporter;
import com.Controller.Reporter.SubmitBooleanReporter;
import com.Controller.Reporter.SubmitTagsReporter;
import com.Controller.Strategies.BooleanWIndowStrategy;
import com.Controller.Strategies.HTMLOrImageStrategy;
import com.Controller.Strategies.SubmitTagsStrategy;
import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.Utils.ControllerUtil;
import com.Utils.DBUtil;
import com.Utils.TessUtil;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Set;

public class MainController extends SingleDocumentController {

    @FXML
    private Label inputPathLabel;

    @FXML
    private Label archivePathLabel;

    @FXML
    private Button inputPathChooseButton;

    @FXML
    private Button archivePathChooseButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button processButton;

    @FXML
    private Button archiveButton;

    @FXML
    TextField nameOfProjectTextField;

    @FXML
    TextField searchTermTextField;

    @FXML
    private TableView mainTableView;

    @FXML
    private TableColumn fileNameTableColumn;

    @FXML
    private TableColumn dateTableColumn;

    @FXML
    private TableColumn tagsTableColumn;

    private Reporter reporter;

    @FXML
    private void initialize() {
        archivePathLabel.setText(ObjectHub.getInstance().getProperties().getProperty("localArchivePath"));

        inputPathLabel.setText(ObjectHub.getInstance().getProperties().getProperty("lastInputPath"));

        mainTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Document selectedDocument = (Document) mainTableView.getSelectionModel().getSelectedItem();
                    HTMLOrImageStrategy htmlOrImageStrategy = new HTMLOrImageStrategy(selectedDocument);
                    ControllerUtil.createNewWindow(htmlOrImageStrategy);
                }
            }
        });
    }

    public void prepareTagsBeforeProcessing(){
        Reporter booleanReporter = new SubmitBooleanReporter() {
            @Override
            public void submitBoolean(boolean value) {
                if(!value){
                    process(null);
                }else{
                    getTagsForProcessing();
                }
            }
        };

        ControllerUtil.createNewWindow(new BooleanWIndowStrategy(booleanReporter));
    }


    void getTagsForProcessing(){
        Reporter reporter = new SubmitTagsReporter() {
            @Override
            public void submitTags(Set<String> tagSet) {
                process(tagSet);
            }
        };

        ControllerUtil.createNewWindow(new SubmitTagsStrategy(reporter));
    }


    void process(Set<String> tagSet) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Set<Document> documentSet = TessUtil.processFolder(new File(inputPathLabel.getText()), null,mainTableView,
                        new TableColumn[] { fileNameTableColumn, dateTableColumn, tagsTableColumn },
                        new PropertyValueFactory[] { new PropertyValueFactory<Document, String>("originalFileName"),
                                new PropertyValueFactory<Document, String>("date"),
                                new PropertyValueFactory<Document, String>("tags") });

                if(tagSet != null){
                    for(String tag : tagSet){
                        for(Document document : documentSet){
                            DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", '" + tag + "');" );
                        }
                    }
                }
            }
        });
        thread.start();
    }

    public void search() {
        List<Document> documentList = DBUtil.getDocumentsForSearchTerm(searchTermTextField.getText());
        ObservableList<Document> documentObservableList = ControllerUtil
                .createObservableList(documentList);
        ControllerUtil.fillTable(mainTableView, documentObservableList,
                new TableColumn[] { fileNameTableColumn, dateTableColumn, tagsTableColumn },
                new PropertyValueFactory[] { new PropertyValueFactory<Document, String>("originalFileName"),
                        new PropertyValueFactory<Document, String>("date"),
                        new PropertyValueFactory<Document, String>("tagString") });
    }

    public void chooseButtonInputPath() {
        String inputPath = choosePath(inputPathLabel);
        ObjectHub.getInstance().getProperties().setProperty("lastInputPath", inputPath);
    }

    public void archiveButtonInputPath() {
        String archivePath = choosePath(archivePathLabel);
        ObjectHub.getInstance().getProperties().setProperty("localArchivePath", archivePath);
    }

    private String choosePath(Label label) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if(!label.getText().equals("")){
            directoryChooser.setInitialDirectory(new File(label.getText()));
        }

        directoryChooser.setTitle("Wähle einen Ordner...");

        File chosenDir = directoryChooser.showDialog(new Stage());

        label.setText(chosenDir.getAbsolutePath());
        return chosenDir.getAbsolutePath();
    }

    public void archive() {
        ObjectHub.getInstance().getArchiver().archive(nameOfProjectTextField.getText());
    }

    @Override
    public Document getDocument() {
        return null;
    }

    @Override
    public void setDocument(Document document) {


    }


    public Reporter getReporter() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    void closeWindow() {

    }
}
