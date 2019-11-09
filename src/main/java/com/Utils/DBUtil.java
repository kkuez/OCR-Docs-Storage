package com.Utils;

import com.Misc.TaskHandling.Strategies.SimpleCalendarOneTimeStrategy;
import com.Misc.TaskHandling.Strategies.TaskStrategy;
import com.Misc.TaskHandling.Task;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.Image;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DBUtil {

    private static Connection connection = null;

    static File dbFile = new File(ObjectHub.getInstance().getProperties().getProperty("dbPath"));

    public static Document lastProcessedDoc;

    public static List<Document> getDocumentsForSearchTerm(String searchTerm) {
        Map<File, Document> documentMap = new HashMap<>();

        DBUtil.showDocumentsFromSQLExpression("select * from Documents where content like '%" + searchTerm + "%'").forEach(document -> {
            document.setTagSet(getTagsForDocument(document));
            documentMap.put(document.getOriginFile(), document);
        });

        List<Document> taggedDocuments = getDocumentsByTag(searchTerm);
        taggedDocuments.forEach(document -> documentMap.putIfAbsent(document.getOriginFile(), document));
        List<Document> documentList = new ArrayList<>();
        documentList.addAll(documentMap.values());
        ObjectHub.getInstance().getArchiver().setDocumentList(documentList);
        return documentList;
    }

    public static Document getDocumentForID(int id) {
        return DBUtil.showDocumentsFromSQLExpression("select * from Documents where id =" + id + "").get(0);
    }

    public static Set<String> getFilePathOfDocsContainedInDB() {
        Set<String> filePathSet = new HashSet<>();
        List<Document> documentList = showDocumentsFromSQLExpression("select * from Documents");
        documentList.forEach(document -> filePathSet.add(document.getOriginFile().getAbsolutePath()));
        return filePathSet;
    }

    public static Map<Integer, User> getAllowedUsersMap(){
        Statement statement = null;
        Map<Integer, User> userMap = new HashMap<>();
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from AllowedUsers");
            while (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("name"));
                userMap.put(rs.getInt("id"), user);
            }
            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("select * from AllowedUsers", e);
        }

        return userMap;
    }

    public static List<String> getShoppingListFromDB(){
        List<String> shoppingList = new ArrayList<>();
        Statement statement = null;
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM ShoppingList");
            while (rs.next()) {
                shoppingList.add(rs.getString("item"));
            }
            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("SELECT * FROM ShoppingList", e);
        }
        return shoppingList;
    }

    public static List<String> getStandardListFromDB(){
        List<String> standardList = new ArrayList<>();
        Statement statement = null;
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM StandardList");
            while (rs.next()) {
                standardList.add(rs.getString("item"));
            }
            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("SELECT * FROM StandardList", e);
        }
        return standardList;
    }

  public static void insertDocumentToDB(Document document){
        if(document.getClass().equals(Image.class)){
            lastProcessedDoc = document;
        }
        DBUtil.executeSQL(document.getInsertDBString());
    }

    public static void removeTask(Task task){
        int year = task.getTime().getYear();
        int month = task.getTime().getMonth().getValue();
        int day = task.getTime().getDayOfMonth();
        int hour = task.getTime().getHour();
        int minute = task.getTime().getMinute();

        executeSQL("delete from CalendarTasks where name='" + task.getName() + "' AND year=" + year + " AND month=" + month + " AND day=" + day + " AND hour=" + hour + " AND minute=" + minute);
    }

    public static void removeLastProcressedDocument(){
        executeSQL("delete from Documents where id=" + lastProcessedDoc.getId() + "");
        executeSQL("delete from Bons where belongsToDocument=" + lastProcessedDoc.getId() + "");
        FileUtils.deleteQuietly(lastProcessedDoc.getOriginFile());
    }

    public static Set<String> getTagsForDocument(Document document){
        Set<String> tagSet = new HashSet<>();
        Statement statement = null;
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT Tag FROM Tags where belongsToDocument=" + document.getId());
            while (rs.next()) {
                tagSet.add(rs.getString("Tag"));
            }
            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("SELECT Tag FROM Tags where belongsToDocument=" + document.getId(), e);
        }
        return tagSet;
    }

    public static void executeSQL(String sqlStatement) {
        try {
            Statement statement = getConnection().createStatement();
            statement.executeUpdate(sqlStatement);
            statement.close();
        } catch (SQLException e) {
            LogUtil.logError(sqlStatement, e);
        }
    }

    public static float getSumMonth(String monthAndYear, User userOrNull){
        List<Document> documentList = getDocumentsForMonthAndYear(monthAndYear);

        Map<Integer, Float> bonIdMap = new HashMap<>();
        getBonsfromDB().forEach(bon -> bonIdMap.put(bon.getBelongsToDocument(), bon.getSum()));

        float resultSum = 0f;

        for(Document document : documentList){
            if(bonIdMap.keySet().contains(document.getId())){
                if(userOrNull == null) {
                    resultSum += bonIdMap.get(document.getId());
                }else{
                    resultSum += userOrNull.getId() == document.getUser() ? bonIdMap.get(document.getId()) : 0;
                }
            }
        }
        return resultSum;
    }

    public static List<Document> getDocumentsForMonthAndYear(String monthAndYear){
        List<Document> documentList = new ArrayList<>();
        Statement statement = null;
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Documents WHERE date like '%" + monthAndYear.replace("-", ".") + "%' AND originalFile like '%Bons%'");
            documentList = new ArrayList<>();
            while (rs.next()) {
                Document document = new Image(rs.getString("content"), new File(rs.getString("originalFile")), rs.getInt("id"));
                document.setUser(rs.getInt("user"));
                documentList.add(document);
            }
            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("SELECT * FROM Documents WHERE date like '%" + monthAndYear + "%' AND originalFile like '%Bons%'", e);
        }
        return documentList;
    }

    public static boolean isFilePresent(File newFile){
        int filesSizeOfNewFile = countDocuments("Documents" ,"where sizeOfOriginalFile=" + FileUtils.sizeOf(newFile));

        return filesSizeOfNewFile > 0;
    }

    public static List<Task> getTasksFromDB(Bot bot){
        List<Task> taskList = new ArrayList<>();
        try {
            Statement statement = getConnection().createStatement();
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from CalendarTasks");
            while (rs.next()) {
                LocalDateTime time = LocalDateTime.of(rs.getInt("year"),rs.getInt("month"),rs.getInt("day"),rs.getInt("hour"),rs.getInt("minute"));
                String strategyType = rs.getString("strategyType");
                List<User> userList = new ArrayList<>();
                if(rs.getString("user").equals("ALL")){
                    userList.addAll(getAllowedUsersMap().values());
                }else{
                    userList.add(getAllowedUsersMap().get(Integer.parseInt(rs.getString("user"))));
                }

                Task task = new Task(userList, bot, time, rs.getString("name"));
                switch (strategyType){
                    case "SimpleCalendarOneTimeStrategy":
                        TaskStrategy taskStrategy = new SimpleCalendarOneTimeStrategy(task);
                        task.setTaskStrategy(taskStrategy);
                        break;
                }
                taskList.add(task);
            }
            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("select * from Task", e);
        }
        return taskList;
    }

    public static void insertTaskToDB(Task task){
        DBUtil.executeSQL(task.getInsertDBString());
    }

     public static List<Bon> getBonsfromDB(){

         List<Bon> bonSet = new ArrayList<>();
        try {
            Statement statement = getConnection().createStatement();
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from Bons");
            while (rs.next()) {
                bonSet.add(new Bon(rs.getInt("belongsToDocument"), rs.getFloat("sum")));
            }
            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("select * from Bons", e);
        }
        return bonSet;
    }


    public static List<Document> getDocumentsByTag(String tag){
        Statement statement = null;
        List<Integer> documentIds = new ArrayList<>();
        List<Document> documentList = new ArrayList<>();
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select belongsToDocument from Tags where Tag like '%" + tag + "%'");
            while (rs.next()) {
                documentIds.add(rs.getInt("belongsToDocument"));
            }

            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("select belongsToDocument from Tags where Tag like '%" + tag + "%'", e);
        }
        for(Integer id : documentIds){
            documentList.addAll(DBUtil
                    .showDocumentsFromSQLExpression("select * from Documents where id=" + id + ""));
        }
        return documentList;

    }

    public static List<Document> showDocumentsFromSQLExpression(String sqlExpression) {
        Statement statement = null;
        List<Document> documentList = new ArrayList<>();
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery(sqlExpression);
            documentList = new ArrayList<>();
            while (rs.next()) {
                //TODO auch pdfs eigene klasse schreiben
                Image image = new Image(rs.getString("content"), new File(rs.getString("originalFile")), rs.getInt("id"));
                image.setTagSet(getTagsForDocument(image));
                documentList.add(image);
            }

            statement.close();
        } catch (SQLException e) {
            LogUtil.logError(sqlExpression, e);
        }
        return documentList;
    }

    public static int countDocuments(String tableName, String sqlAddition){
        Statement statement = null;
        int count = 0;
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + tableName + " " + sqlAddition);
            while (rs.next()) {
                count = rs.getInt("Count(*)");
            }

            statement.close();
        } catch (SQLException e) {
            LogUtil.logError("SELECT COUNT(*) FROM " + tableName + " " + sqlAddition, e);
        }
        return count;
    }

    private static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() && dbFile.exists()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            }
        } catch (SQLException e) {
            LogUtil.logError("jdbc:sqlite:" + dbFile.getAbsolutePath(), e);
        }

        return connection;
    }
}
