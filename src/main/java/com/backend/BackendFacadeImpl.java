package com.backend;

import com.TasksRunnable;
import com.backend.taskhandling.Task;
import com.data.Bon;
import com.data.Memo;
import com.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

@Service
public class BackendFacadeImpl implements BackendFacade {

    @Autowired
    DBDAO dbdao;
    @Autowired
    private Archiver archiver;
    @Autowired
    private ObjectHub objectHub;

    @Override
    public void insertBon(Bon bon) {
        dbdao.insertBon(bon);
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

    public void deleteTask(UUID uuid) {
        dbdao.deleteTask(uuid);
    }

    @Override
    public void insertShoppingItem(String item) {
        dbdao.insertShoppingItem(item);
    }

    @Override
    public int getIdForNextDocument() {
        return dbdao.countDocuments("Documents", "");
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
    public Map<String, User> getAllowedUsers() {
        return dbdao.getAllowedUsersMap(this);
    }

    @Override
    public float getSumMonth(LocalDate yearMonth, User userOrNull) {
        return dbdao.getSumMonth(yearMonth, userOrNull);
    }

    @Override
    public TasksRunnable getTasksRunnable() {
        return getTasksRunnable();
    }

    @Override
    public List<Task> getTasks(String userid) {
        List<Task> tasksFromDB = dbdao.getTasksFromDB(this, Optional.of(userid));
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
    public List<Bon> getLastBons(String userid, Integer lastMany) {
        return dbdao.getLastBons(userid, lastMany);
    }

    @Override
    public void delete(String userid, UUID uuid) {
        dbdao.deleteBon(userid, uuid);
    }

    @Override
    public void insertMemo(Memo memo) {
        dbdao.insertMemo(memo);
    }

    @Override
    public List<Memo> getMemos(User userid) {
        return dbdao.getMemos(userid);
    }

    @Override
    public DBDAO getDBDAO() {
        return dbdao;
    }

    @Override
    public ObjectHub getObjectHub() {
        return objectHub;
    }

    @Override
    public void deleteMemo(List<Memo> memos, List<User> users) {
        dbdao.deleteMemos(memos, users);
    }
}
