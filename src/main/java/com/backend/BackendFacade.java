package com.backend;

import com.TasksRunnable;
import com.backend.taskhandling.Task;
import com.data.Bon;
import com.data.Memo;
import com.data.User;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BackendFacade {

    void insertBon(Bon bon);

    int getIdForNextDocument();

    void insertTask(Task task);

    List<Task> getTasks();

    void deleteTask(Task task);

    void deleteTask(UUID uuid);

    void insertShoppingItem(String item);

    List<String> getShoppingList();

    void deleteFromShoppingList(String itemName);

    Map<String, User> getAllowedUsers();

    float getSumMonth(LocalDate yearMonth, User userOrNull);

    TasksRunnable getTasksRunnable();
    
    List<Task> getTasks(String userid);

    Float getSum(String userid);

    File copyToArchive(File newPic, boolean isBon);

    List<Bon> getLastBons(String userid, Integer lastMany);

    void delete(String userid, UUID sum);

    void insertMemo(Memo memo);

    List<Memo> getMemos(User userid);
}
