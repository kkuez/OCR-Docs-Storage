package com.backend;

import com.backend.taskHandling.Task;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.Image;
import com.objectTemplates.User;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BackendFacade {

    void insertQRItem(int slot, String itemName);

    Map<Integer, String> getQRItems();


    void insertDocument(Document document);

    void deleteLastDocument();

    int getIdForNextDocument();

    //No special getsum method since it has to be calculated from the Client

    List<Bon> getBonsForMonth(LocalDate targetYearMonth);


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

    Set<String> getFilePathOfDocsContainedInDB();

    boolean isFilePresent(File file);

    void insertTag(int documentId, String tag);

    void insertPicture(Image image);

    List<Document> getDocuments(String searchTerm);

    Map<Integer, User> getAllowedUsers();

    Document getDocument(int id);

    void updateDocument(Document document);

    void updateQRItem(Integer itemNumber, String itemName);

    void insertUserToAllowedUsers(Integer id, String firstName, Long chatId);

    float getSumMonth(LocalDate yearMonth, User userOrNull);
}
