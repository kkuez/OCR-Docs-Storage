package com.utils;

import com.gui.controller.strategies.ControllerStrategy;
import com.objectTemplates.Document;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class ControllerUtil {

    private ControllerUtil() {}

    public static ObservableList<Document> createObservableList(List<Document> documentList) {
        ObservableList<Document> observableList = FXCollections.observableArrayList();
        observableList.addAll(documentList);
        return observableList;
    }

    public static void fillTable(TableView tableView, ObservableList<Document> observableList,
            TableColumn[] tableColumns, PropertyValueFactory[] propertyValueFactories) {
        for (int i = 0; i < tableColumns.length; i++) {
            tableColumns[i].setCellValueFactory(propertyValueFactories[i]);
        }

        Platform.runLater(() -> {
            tableView.getColumns().clear();
            tableView.setItems(observableList);
            tableView.getColumns().addAll(tableColumns);
        });
    }

    public static void createNewWindow(ControllerStrategy controllerStrategy) {
        Stage stage = controllerStrategy.getPreparedStage();
        stage.show();
    }
}
