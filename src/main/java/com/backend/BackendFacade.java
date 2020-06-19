package com.backend;

import com.objectTemplates.Appointment;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.Image;
import com.utils.TimeUtil;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BackendFacade {

    void insertQRItem(int slot, String itemName);

    Map<Integer, String> getQRItems();


    void insertBon(Bon bon);

    //No special getsum method since it has to be calculated from the Client

    Map<Long, Bon> getBonsForMonth(LocalDate targetYearMonth);

    void deleteLastBon();


    File getPDF(LocalDate start, LocalDate end);

    File getLogs();


    void insertAppointment(Appointment appointment);

    List<Appointment> getAppointments();

    void deleteAppointment(Appointment appointment);


    void insertShoppingItem(String item);

    List<String> getShoppingList();

    void deleteFromShoppingList(String itemName);

    void insertToStandartList(String item);

    List<String> getStandartList();

    void deleteFromStandartList(String itemName);


    void insertMemo(String itemName);

    List<String> getMemos();

    void deleteMemo(String memoName);



    void insertPicture(Image image);

    void deleteLastPicture();

    List<Document> getDocuments(String searchTerm);

}
