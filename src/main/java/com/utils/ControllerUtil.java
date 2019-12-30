package com.utils;

import com.Main;
import com.controller.strategies.ControllerStrategy;
import com.objectTemplates.Document;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.util.List;

public class ControllerUtil {
    private static Logger logger = Main.getLogger();

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

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                tableView.getColumns().clear();
                tableView.setItems(observableList);
                tableView.getColumns().addAll(tableColumns);
            }
        });
    }

    public static void createNewWindow(ControllerStrategy controllerStrategy) {
        Stage stage = controllerStrategy.getPreparedStage();
        stage.show();
    }
}
