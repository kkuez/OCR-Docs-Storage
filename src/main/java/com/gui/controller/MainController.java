package com.gui.controller;

import com.backend.BackendFacade;
import com.gui.controller.reporter.*;
import com.gui.controller.strategies.*;
import com.backend.ObjectHub;
import com.objectTemplates.Document;
import com.objectTemplates.User;
import com.utils.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

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
            public void addStep( Update updateOrNull) {
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

    public void prepareTagsBeforeProcessing(){
        logger.info(GUI_INIT_STRING + "Process new Files from Folder: " + inputPathLabel);
        Reporter booleanReporter = (SubmitBooleanReporter) value -> {
            if(!value){
                process(null);
            }else{
                getTagsForProcessing();
            }
        };

        ControllerUtil.createNewWindow(new BooleanWIndowStrategy(booleanReporter));
    }


    void getTagsForProcessing(){
        Reporter reporter = (SubmitTagsReporter) tagSet -> process(tagSet);

        ControllerUtil.createNewWindow(new SubmitTagsStrategy(reporter));
    }


    void process(Set<String> tagSet) {
        ObjectHub.getInstance().getExecutorService().submit(() -> {
            Set<Document> documentSet = TessUtil.processFolder(mainTableView,
                    new TableColumn[] { fileNameTableColumn, dateTableColumn, tagsTableColumn },
                    new PropertyValueFactory[] { new PropertyValueFactory<Document, String>("originalFileName"),
                            new PropertyValueFactory<Document, String>("date"),
                            new PropertyValueFactory<Document, String>("tags") }, (ProgressReporter) progressReporter, facade);

            if(tagSet != null){
                for(String tag : tagSet){
                    for(Document document : documentSet){
                        facade.insertTag(document.getId(), tag);
                    }
                }
            }
        });
        }

    public void search() {
        logger.info(GUI_INIT_STRING + "Performing search with Term '" + searchTermTextField.getText() + "'");
        List<Document> documentList = facade.getDocuments(searchTermTextField.getText());
        ObservableList<Document> documentObservableList = ControllerUtil
                .createObservableList(documentList);
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
        if(!label.getText().equals("")){
            directoryChooser.setInitialDirectory(new File(label.getText()));
        }

        directoryChooser.setTitle("Wähle einen Ordner...");

        File chosenDir = directoryChooser.showDialog(new Stage());

        label.setText(chosenDir.getAbsolutePath());
        return chosenDir.getAbsolutePath();
    }

    public void archive() {
        logger.info(GUI_INIT_STRING + "Archive "  + nameOfProjectTextField.getText());
        ObjectHub.getInstance().getArchiver().archive(nameOfProjectTextField.getText());
    }

    public void createPDF(){
        ChooseTimeReporter chooseTimeReporter = (beginDate, endDate) -> {

            try(PDDocument document = new PDDocument()) {
                PDPage firstPage = new PDPage();
                document.addPage(firstPage);
                PDPageContentStream pdPageContentStream = new PDPageContentStream(document, firstPage);
                pdPageContentStream.beginText();
                pdPageContentStream.setFont( PDType1Font.COURIER_BOLD, 24 );
                pdPageContentStream.setLeading(14.5f);
                pdPageContentStream.newLineAtOffset(25, 725);
                pdPageContentStream.showText("Zusammenfassung " + beginDate.toString() + " - " + endDate.toString());
                List<LocalDate> relatedMonth = new LinkedList<>();
                relatedMonth.add(beginDate);
                int index = -1;
                pdPageContentStream.setFont( PDType1Font.COURIER, 16 );
                LocalDate nextLocalDate = beginDate.withDayOfMonth(1);
                Map<User, Float> userSumMap = new HashMap<>();
                facade.getAllowedUsers().values().forEach(user -> userSumMap.put(user, 0f));
                pdPageContentStream.newLine();
                float sumOfAll = 0f;
                do{
                    index++;
                    nextLocalDate = beginDate.plusMonths(index).withDayOfMonth(TimeUtil.getdaysOfMonthCount(beginDate.plusMonths(index).getYear(), beginDate.plusMonths(index).getMonth().getValue()));
                    pdPageContentStream.newLine();
                    pdPageContentStream.newLine();
                    pdPageContentStream.setFont( PDType1Font.COURIER_BOLD, 16 );
                    pdPageContentStream.showText(nextLocalDate.toString());
                    pdPageContentStream.setFont( PDType1Font.COURIER, 16 );
                    float sumForMonth = 0f;
                    for(Map.Entry<User, Float> entry : userSumMap.entrySet()){

                        pdPageContentStream.newLine();
                        float sumForUser = facade.getSumMonth(nextLocalDate, entry.getKey());
                        sumForMonth += sumForUser;
                        sumOfAll += sumForUser;
                        userSumMap.put(entry.getKey(), entry.getValue() + sumForUser);
                        pdPageContentStream.showText(entry.getKey().getName() + ": " + sumForUser);
                    }

                    pdPageContentStream.newLine();
                    pdPageContentStream.showText("Gesamt: " + sumForMonth);
                }while(!nextLocalDate.withDayOfMonth(1).toString().equals(endDate.withDayOfMonth(1).toString()));

                pdPageContentStream.newLine();
                pdPageContentStream.newLine();
                pdPageContentStream.showText("Alles in allem: " + sumOfAll);
                pdPageContentStream.newLine();
                userSumMap.keySet().forEach(user -> {
                    try {
                        pdPageContentStream.showText(user.getName() + ": " + userSumMap.get(user));
                        pdPageContentStream.newLine();
                    } catch (IOException e) {
                        logger.error("Failed activating bot", e);
                    }
                });
                pdPageContentStream.endText();
                pdPageContentStream.close();
                File fileToSave = new File(beginDate.toString().replace('\'', '_') + " - " + endDate.toString().replace('\'', '_') + ".pdf");
                document.save(fileToSave);
                Desktop.getDesktop().open(fileToSave);
            } catch (IOException e) {
                logger.error("Failed activating bot", e);
            }
        };

        ControllerStrategy pdfControllerStrategy = new ChooseTimeStrategy(chooseTimeReporter);
        ControllerUtil.createNewWindow(pdfControllerStrategy);
    }

    @Override
    public Document getDocument() {
        //Fixme these methods are leftovers of the parent class -> remove somehow
        return null;
    }

    @Override
    public void setDocument(Document document) {

    }

    @Override
    void closeWindow() {

    }
}