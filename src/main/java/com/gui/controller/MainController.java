package com.gui.controller;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.backend.BackendFacade;
import com.backend.ObjectHub;
import com.gui.controller.reporter.ProgressReporter;
import com.gui.controller.reporter.Reporter;
import com.gui.controller.reporter.SubmitBooleanReporter;
import com.gui.controller.reporter.SubmitTagsReporter;
import com.gui.controller.strategies.BooleanWIndowStrategy;
import com.gui.controller.strategies.HTMLOrImageStrategy;
import com.gui.controller.strategies.SubmitTagsStrategy;
import com.objectTemplates.Document;
import com.utils.ControllerUtil;
import com.utils.TessUtil;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

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
    private Button pdfButton;

    @FXML
    private TextField nameOfProjectTextField;

    @FXML
    private TextField searchTermTextField;

    @FXML
    private TableView mainTableView;

    @FXML
    private TableColumn fileNameTableColumn;

    @FXML
    private TableColumn dateTableColumn;

    @FXML
    private TableColumn tagsTableColumn;

    @FXML
    private ProgressIndicator progressIndicator;

    private Reporter progressReporter;

    private static String GUI_INIT_STRING = "Gui: ";

    private BackendFacade facade;

    @FXML
    private void initialize() {
        progressReporter = new ProgressReporter() {

            @Override
            public void setTotalSteps(int steps, Update updateOrNull) {
                progressManager.setTotalSteps(steps);
            }

            @Override
            public void addStep(Update updateOrNull) {
                progressManager.addStep();
                Platform.runLater(() -> progressIndicator.setProgress(progressManager.getCurrentProgress()));
            }

            @Override
            public void setStep(int step, Update updateOrNull) {
                progressManager.setCurrentStep(step);
                Platform.runLater(() -> progressIndicator.setProgress(progressManager.getCurrentProgress()));
            }
        };

        facade = ObjectHub.getInstance().getFacade();
        logger.info(GUI_INIT_STRING + "Init Gui.");
        archivePathLabel.setText(ObjectHub.getInstance().getArchiver().getArchiveFolder().getAbsolutePath());
        inputPathLabel.setText(ObjectHub.getInstance().getProperties().getProperty("lastInputPath"));
        mainTableView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                Document selectedDocument = (Document) mainTableView.getSelectionModel().getSelectedItem();
                HTMLOrImageStrategy htmlOrImageStrategy = new HTMLOrImageStrategy(selectedDocument);
                ControllerUtil.createNewWindow(htmlOrImageStrategy);
            }
        });
    }

    public void prepareTagsBeforeProcessing() {
        logger.info(GUI_INIT_STRING + "Process new Files from Folder: " + inputPathLabel);
        Reporter booleanReporter = (SubmitBooleanReporter) value -> {
            if (!value) {
                process(null);
            } else {
                getTagsForProcessing();
            }
        };

        ControllerUtil.createNewWindow(new BooleanWIndowStrategy(booleanReporter));
    }

    void getTagsForProcessing() {
        Reporter reporter = (SubmitTagsReporter) tagSet -> process(tagSet);

        ControllerUtil.createNewWindow(new SubmitTagsStrategy(reporter));
    }

    void process(Set<String> tagSet) {
        ObjectHub.getInstance().getExecutorService().submit(() -> {
            Set<Document> documentSet = TessUtil.processFolder(mainTableView,
                    new TableColumn[] { fileNameTableColumn, dateTableColumn, tagsTableColumn },
                    new PropertyValueFactory[] { new PropertyValueFactory<Document, String>("originalFileName"),
                            new PropertyValueFactory<Document, String>("date"),
                            new PropertyValueFactory<Document, String>("tags") },
                    (ProgressReporter) progressReporter, facade);

            if (tagSet != null) {
                for (String tag : tagSet) {
                    for (Document document : documentSet) {
                        facade.insertTag(document.getId(), tag);
                    }
                }
            }
        });
    }

    public void search() {
        logger.info(GUI_INIT_STRING + "Performing search with Term '" + searchTermTextField.getText() + "'");
        List<Document> documentList = facade.getDocuments(searchTermTextField.getText());
        ObservableList<Document> documentObservableList = ControllerUtil.createObservableList(documentList);
        ControllerUtil.fillTable(mainTableView, documentObservableList,
                new TableColumn[] { fileNameTableColumn, dateTableColumn, tagsTableColumn },
                new PropertyValueFactory[] { new PropertyValueFactory<Document, String>("originalFileName"),
                        new PropertyValueFactory<Document, String>("date"),
                        new PropertyValueFactory<Document, String>("tags") });
    }

    public void chooseButtonInputPath() {
        String inputPath = choosePath(inputPathLabel);
        ObjectHub.getInstance().getProperties().setProperty("lastInputPath", inputPath);
    }

    private String choosePath(Label label) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (!label.getText().equals("")) {
            directoryChooser.setInitialDirectory(new File(label.getText()));
        }

        directoryChooser.setTitle("Wähle einen Ordner...");

        File chosenDir = directoryChooser.showDialog(new Stage());

        label.setText(chosenDir.getAbsolutePath());
        return chosenDir.getAbsolutePath();
    }

    public void archive() {
        logger.info(GUI_INIT_STRING + "Archive " + nameOfProjectTextField.getText());
        ObjectHub.getInstance().getArchiver().archive(nameOfProjectTextField.getText());
    }

    @Override
    public Document getDocument() {
        // Fixme these methods are leftovers of the parent class -> remove somehow
        return null;
    }

    @Override
    public void setDocument(Document document) {

    }

    @Override
    void closeWindow() {

    }
}
