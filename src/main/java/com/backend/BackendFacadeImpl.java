package com.backend;

import com.TasksRunnable;
import com.backend.taskhandling.Task;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.User;
import com.utils.PDFUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class BackendFacadeImpl implements BackendFacade {

    @Autowired
    DBDAO dbdao;
    @Autowired
    private Archiver archiver;
    @Autowired
    private ObjectHub objectHub;

    @Override
    public void updateQRItem(Integer itemNumber, String itemName) {
        dbdao.updateQRItem(itemNumber, itemName);
    }

    @Override
    public boolean hasXORKey(Integer userID) {
        return dbdao.hasXORKey(userID);
    }

    @Override
    public void setUserHasXORKey(Integer userID, boolean has) {
        dbdao.setUserHasXORKey(userID, has);
    }

    @Override
    public String getXORKey() {
        return dbdao.getXORKey();
    }

    @Override
    public void setXORKey(String key) {
        dbdao.setXORKey(key);
    }

    @Override
    public LocalDate getLastKeyRenewalDate() {
        return dbdao.getLastKeyRenewalDate();
    }

    @Override
    public ExecutorService getExecutorService() {
        return objectHub.getExecutorService();
    }

    @Override
    public void insertBon(Bon bon) {
        dbdao.insertBon(bon);
    }

    @Override
    public void insertDocument(Document document) {
        dbdao.insertDocument(document);
    }

    @Override
    public void updateDocument(Document document) {
        dbdao.updateDocument(document);
    }

    @Override
    public Document getDocument(int id) {
        return dbdao.getDocument(id);
    }

    @Override
    public List<Bon> getSum(LocalDate targetYearMonth) {
        return dbdao.getBonsForMonth(targetYearMonth.getYear(), targetYearMonth.getMonthValue());
    }

    @Override
    public File getPDF(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            final List<Bon> allBons = dbdao.getAllBons();
            final List<LocalDate> dates = allBons.stream().map(bon -> {
                final String[] splitDate = bon.getDate().split("\\.");
                String dayFormat = splitDate[0].length() == 1 ? "d" : "dd";
                String monthFormat = splitDate[1].length() == 1 ? "M" : "MM";
                return LocalDate.parse(bon.getDate(),
                        DateTimeFormatter.ofPattern(dayFormat + "." + monthFormat + ".yyyy"));
            }).collect(Collectors.toList());
            dates.sort(LocalDate::compareTo);
            return PDFUtil.createPDF(this, dates.get(0), LocalDate.now());
        } else {
            return PDFUtil.createPDF(this, start, end);
        }
    }

    @Override
    public File getLogs() {
        // TODO whole new feature, first do the other stuff plox
        return null;
    }

    @Override
    public void insertTask(Task task) {
        dbdao.insertTaskToDB(task);
    }

    @Override
    public List<Task> getTasks() {
        return dbdao.getTasksFromDB(this);
    }

    @Override
    public void deleteTask(Task task) {
        dbdao.deleteTask(task);
    }

    @Override
    public void deleteTask(UUID uuid) {
        dbdao.deleteTask(uuid);
    }

    @Override
    public void insertShoppingItem(String item) {
        dbdao.insertShoppingItem(item);
    }

    @Override
    public List<String> getShoppingList() {
        return dbdao.getShoppingListFromDB();
    }

    @Override
    public void deleteFromShoppingList(String itemName) {
        dbdao.deleteFromShoppingList(itemName);
    }

    @Override
    public void insertToStandartList(String item) {
        dbdao.insertToStandartList(item);
    }

    @Override
    public List<String> getStandartList() {
        return dbdao.getStandardListFromDB();
    }

    @Override
    public void deleteFromStandartList(String itemName) {
        dbdao.deleteFromStandartList(itemName);
    }

    @Override
    public void insertMemo(String itemName, long userId) {
        dbdao.insertMemo(itemName, userId);
    }

    @Override
    public List<String> getMemos(long userId) {
        return dbdao.getMemos(userId);
    }

    @Override
    public void deleteMemo(String memoName) {
        dbdao.deleteMemo(memoName);
    }

    @Override
    public Set<String> getFilePathOfDocsContainedInDB() {
        return dbdao.getFilePathOfDocsContainedInDB();
    }

    @Override
    public boolean isFilePresent(File file) {
        return dbdao.isFilePresent(file);
    }

    @Override
    public void insertTag(int documentId, String tag) {
        dbdao.insertTag(documentId, tag);
    }

    @Override
    public int getIdForNextDocument() {
        return dbdao.countDocuments("Documents", "");
    }

    @Override
    public List<Document> getDocuments(String searchTerm) {
        return dbdao.getDocumentsForSearchTerm(searchTerm);
    }

    @Override
    public Map<String, User> getAllowedUsers() {
        return dbdao.getAllowedUsersMap(this);
    }

    @Override
    public void insertUserToAllowedUsers(Integer id, String firstName, Long chatId) {
        dbdao.insertUserToAllowedUsers(id, firstName, chatId);
    }

    @Override
    public float getSumMonth(LocalDate yearMonth, User userOrNull) {
        return dbdao.getSumMonth(yearMonth, userOrNull);
    }

    @Override
    public File getBonFolder() {
        return archiver.getBonFolder();
    }

    @Override
    public TasksRunnable getTasksRunnable() {
        return getTasksRunnable();
    }

    @Override
    public List<Task> getTasks(String userid) {
        List<Task> tasksFromDB = dbdao.getTasksFromDB(this, userid);
        Collections.reverse(tasksFromDB);
        return tasksFromDB;
    }

    @Override
    public Float getSum(String userid) {
        return dbdao.getSum(userid);
    }

    @Override
    public File copyToArchive(File newPic, boolean isBon) {
        return archiver.copyToArchive(newPic, isBon);
    }

    @Override
    public List<Float> getLastBons(String userid, Integer lastMany) {
        return dbdao.getLastSums(userid, lastMany);
    }

    @Override
    public void delete(String userid, float sum) {
        dbdao.deleteBon(userid, sum);
    }
}
