package com.backend;

import com.backend.taskHandling.Task;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.Image;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FacadeController implements BackendFacade {

    DBDAO dbdao;

    FacadeController() {
        dbdao = new DBDAO();
    }

    @Override
    public void insertQRItem(int itemNumber, String itemName) {
        dbdao.updateQRItem(itemNumber, itemName);
    }

    @Override
    public Map<Integer, String> getQRItems() {
        return dbdao.getQRItemMap();
    }

    @Override
    public void insertBon(Bon bon) {

    }

    @Override
    public List<Bon> getBonsForMonth(LocalDate targetYearMonth) {
        //TODO
        dbdao.getBonsForMonth(targetYearMonth.getYear(), targetYearMonth.getMonthValue());
        return null;
    }

    @Override
    public void insertBon(int document, int sum, Image image) {
        //TODO
    }

    @Override
    public File getPDF(LocalDate start, LocalDate end) {
        //TODO
        return null;
    }

    @Override
    public File getLogs() {
        //TODO whole new feature, first do the other stuff plox
        return null;
    }

    @Override
    public void insertTask(Task task) {
        dbdao.insertTaskToDB(task);
    }

    @Override
    public List<Task> getTasks() {
        return dbdao.getTasksFromDB();
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
    public void insertPicture(Image image) {

    }

    @Override
    public void deleteLastDocument() {
        dbdao.removeLastProcressedDocument();
    }

    @Override
    public List<Document> getDocuments(String searchTerm) {
        return null;
    }
}
