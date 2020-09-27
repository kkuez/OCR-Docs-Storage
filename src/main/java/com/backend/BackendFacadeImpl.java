package com.backend;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.backend.taskhandling.Task;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.User;
import com.utils.PDFUtil;

public class BackendFacadeImpl implements BackendFacade {

    DBDAO dbdao;

    BackendFacadeImpl(File dbFile) {
        dbdao = new DBDAO(dbFile);
    }

    @Override
    public void insertQRItem(int itemNumber, String itemName) {
        dbdao.updateQRItem(itemNumber, itemName);
    }

    @Override
    public void updateQRItem(Integer itemNumber, String itemName) {
        dbdao.updateQRItem(itemNumber, itemName);
    }

    @Override
    public Map<Integer, String> getQRItems() {
        return dbdao.getQRItemMap();
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
    public List<Bon> getBonsForMonth(LocalDate targetYearMonth) {
        return dbdao.getBonsForMonth(targetYearMonth.getYear(), targetYearMonth.getMonthValue());
    }

    @Override
    public File getPDF(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            final List<Bon> allBons = dbdao.getAllBons();
            final List<LocalDate> dates = allBons.stream()
                    .map(bon -> LocalDate.parse(bon.getDate(), DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .collect(Collectors.toList());
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
        dbdao.removeTask(task);
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
    public void deleteLastDocument() {
        dbdao.removeLastProcressedDocument();
    }

    @Override
    public List<Document> getDocuments(String searchTerm) {
        return dbdao.getDocumentsForSearchTerm(searchTerm);
    }

    @Override
    public Map<Integer, User> getAllowedUsers() {
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
}