package com.Controller;

import com.Controller.Strategies.HTMLOrImageStrategy;
import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.Utils.ControllerUtil;
import com.Utils.DBUtil;
import com.Utils.TessUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.print.Doc;
import java.io.File;
import java.util.List;

public class MainController extends Controller {

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

    @FXML
    private void initialize() {
        archivePathLabel.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                ObjectHub.getInstance().getArchiver().setChosenFolder(new File(archivePathLabel.getText()));
            }
        });
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

    public void process() {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                TessUtil.processFolder(new File(inputPathLabel.getText()), mainTableView,
                        new TableColumn[] { fileNameTableColumn, dateTableColumn, tagsTableColumn },
                        new PropertyValueFactory[] { new PropertyValueFactory<Document, String>("originalFileName"),
                                new PropertyValueFactory<Document, String>("date"),
                                new PropertyValueFactory<Document, String>("tags") });
            }
        });
        thread.start();
    }

    public void search() {
        DBUtil.getFilesForSearchTerm(searchTermTextField.getText());
        ObservableList<Document> documentObservableList = ControllerUtil
                .createObservableList(ObjectHub.getInstance().getArchiver().getDocumentList());
        ControllerUtil.fillTable(mainTableView, documentObservableList,
                new TableColumn[] { fileNameTableColumn, dateTableColumn, tagsTableColumn },
                new PropertyValueFactory[] { new PropertyValueFactory<Document, String>("originalFileName"),
                        new PropertyValueFactory<Document, String>("date"),
                        new PropertyValueFactory<Document, String>("tags") });
    }

    public void chooseButtonInputPath() {
        choosePath(inputPathLabel);
    }

    public void archiveButtonInputPath() {
        choosePath(archivePathLabel);
    }

    private void choosePath(Label label) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wähle einen Ordner...");

        File chosenDir = directoryChooser.showDialog(new Stage());

        label.setText(chosenDir.getAbsolutePath());
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
}
