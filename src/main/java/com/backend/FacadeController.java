package com.backend;

import com.objectTemplates.Appointment;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.Image;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FacadeController implements BackendFacade {
    @Override
    public void insertQRItem(int slot, String itemName) {

    }

    @Override
    public Map<Integer, String> getQRItems() {
        return null;
    }

    @Override
    public void insertBon(Bon bon) {

    }

    @Override
    public Map<Long, Bon> getBonsForMonth(LocalDate targetYearMonth) {
        return null;
    }

    @Override
    public void deleteLastBon() {

    }

    @Override
    public File getPDF(LocalDate start, LocalDate end) {
        return null;
    }

    @Override
    public File getLogs() {
        return null;
    }

    @Override
    public void insertAppointment(Appointment appointment) {

    }

    @Override
    public List<Appointment> getAppointments() {
        return null;
    }

    @Override
    public void deleteAppointment(Appointment appointment) {

    }

    @Override
    public void insertShoppingItem(String item) {

    }

    @Override
    public List<String> getShoppingList() {
        return null;
    }

    @Override
    public void deleteFromShoppingList(String itemName) {

    }

    @Override
    public void insertToStandartList(String item) {

    }

    @Override
    public List<String> getStandartList() {
        return null;
    }

    @Override
    public void deleteFromStandartList(String itemName) {

    }

    @Override
    public void insertMemo(String itemName) {

    }

    @Override
    public List<String> getMemos() {
        return null;
    }

    @Override
    public void deleteMemo(String memoName) {

    }

    @Override
    public void insertPicture(Image image) {

    }

    @Override
    public void deleteLastPicture() {

    }

    @Override
    public List<Document> getDocuments(String searchTerm) {
        return null;
    }
}
