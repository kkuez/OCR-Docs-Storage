package com.backend;

import com.backend.taskHandling.Task;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.Image;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BackendFacade {

    void insertQRItem(int slot, String itemName);

    Map<Integer, String> getQRItems();


    void insertBon(Bon bon);

    //No special getsum method since it has to be calculated from the Client

    List<Bon> getBonsForMonth(LocalDate targetYearMonth);


    void insertBon(int document, int sum, Image image);

    File getPDF(LocalDate start, LocalDate end);

    File getLogs();



    void insertTask(Task task);

    List<Task> getTasks();

    void deleteTask(Task task);


    void insertShoppingItem(String item);

    List<String> getShoppingList();

    void deleteFromShoppingList(String itemName);

    void insertToStandartList(String item);

    List<String> getStandartList();

    void deleteFromStandartList(String itemName);

    void insertMemo(String itemName, long userId);

    List<String> getMemos(long userId);

    void deleteMemo(String memoName);



    void insertPicture(Image image);

    void deleteLastDocument();

    List<Document> getDocuments(String searchTerm);

}
