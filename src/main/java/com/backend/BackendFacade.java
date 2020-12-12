package com.backend;

import com.TasksRunnable;
import com.backend.taskhandling.Task;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.User;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;

public interface BackendFacade {

    boolean hasXORKey(Integer userID);

    void setUserHasXORKey(Integer userID, boolean has);

    String getXORKey();

    void setXORKey(String key);

    LocalDate getLastKeyRenewalDate();

    ExecutorService getExecutorService();

    void insertBon(Bon bon);

    void insertDocument(Document document);

    int getIdForNextDocument();

    // No special getsum method since it has to be calculated from the Client

    List<Bon> getSum(LocalDate targetYearMonth);

    File getPDF(LocalDate start, LocalDate end);

    File getLogs();

    void insertTask(Task task);

    List<Task> getTasks();

    void deleteTask(Task task);

    void deleteTask(UUID uuid);

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

    List<Document> getDocuments(String searchTerm);

    Map<Integer, User> getAllowedUsers();

    Document getDocument(int id);

    void updateDocument(Document document);

    void updateQRItem(Integer itemNumber, String itemName);

    void insertUserToAllowedUsers(Integer id, String firstName, Long chatId);

    float getSumMonth(LocalDate yearMonth, User userOrNull);

    File getBonFolder();

    TasksRunnable getTasksRunnable();
    
    List<Task> getTasks(int userid);

    Float getSum(int userid);

    Float getSum();

    File copyToArchive(File newPic, boolean isBon);
}
