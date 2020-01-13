package com.utils;

import com.Main;
import com.misc.taskHandling.strategies.*;
import com.misc.taskHandling.Task;
import com.ObjectHub;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.Image;
import com.objectTemplates.User;
import com.telegram.Bot;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DBUtil {
    private static Logger logger = Main.getLogger();

    private static Connection connection = null;

    static File dbFile = new File(ObjectHub.getInstance().getProperties().getProperty("dbPath"));

    private static Document lastProcessedDoc = null;

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
        Map<Integer, User> userMap = new HashMap<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from AllowedUsers");) {

            while (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("name"));
                userMap.put(rs.getInt("id"), user);
            }
        } catch (SQLException e) {
            logger.error("select * from AllowedUsers", e);
        }
        return userMap;
    }

    public static List<String> getShoppingListFromDB(){
        List<String> shoppingList = new ArrayList<>();
        try(Statement  statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM ShoppingList");) {

            while (rs.next()) {
                shoppingList.add(rs.getString("item"));
            }
        } catch (SQLException e) {
            logger.error("SELECT * FROM ShoppingList", e);
        }
        return shoppingList;
    }

    public static List<String> getMemoListFromDB(Bot bot, Update update){
        List<String> memoList = new ArrayList<>();
        User user = bot.getNonBotUserFromUpdate(update);
        try(Statement statement = getConnection().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM Memos where user=" + user.getId())) {

            while (rs.next()) {
                memoList.add(rs.getString("item"));
            }
            statement.close();
        } catch (SQLException e) {
            logger.error("SELECT * FROM Memos where user=" + user.getId() + ")", e);
        }
        return memoList;
    }

    public static List<String> getStandardListFromDB(){
        List<String> standardList = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM StandardList");) {

            while (rs.next()) {
                standardList.add(rs.getString("item"));
            }
        } catch (SQLException e) {
            logger.error("SELECT * FROM StandardList", e);
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
        int year = task.getTaskStrategy().getTime().getYear();
        int month = task.getTaskStrategy().getTime().getMonth().getValue();
        int day = task.getTaskStrategy().getTime().getDayOfMonth();
        int hour = task.getTaskStrategy().getTime().getHour();
        int minute = task.getTaskStrategy().getTime().getMinute();

        executeSQL("delete from CalendarTasks where name='" + task.getName() + "' AND year=" + year + " AND month=" + month + " AND day=" + day + " AND hour=" + hour + " AND minute=" + minute);
    }

    public static void removeLastProcressedDocument(){
        executeSQL("delete from Documents where id=" + lastProcessedDoc.getId() + "");
        executeSQL("delete from Bons where belongsToDocument=" + lastProcessedDoc.getId() + "");
        FileUtils.deleteQuietly(lastProcessedDoc.getOriginFile());
    }

    public static Set<String> getTagsForDocument(Document document){
        Set<String> tagSet = new HashSet<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT Tag FROM Tags where belongsToDocument=" + document.getId());) {

            while (rs.next()) {
                tagSet.add(rs.getString("Tag"));
            }
        } catch (SQLException e) {
            logger.error("SELECT Tag FROM Tags where belongsToDocument=" + document.getId(), e);
        }
        return tagSet;
    }

    public static void executeSQL(String sqlStatement) {
        try(Statement statement = getConnection().createStatement();) {
            statement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            logger.error(sqlStatement, e);
        }
    }

    public static float getSumMonth(String monthAndYear, User userOrNull){
        float resultSum = 0f;
        String plusUserString = userOrNull == null ? "" :" AND USER=" + userOrNull.getId();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Documents INNER JOIN Bons ON Documents.id=Bons.belongsToDocument where date like '%" + monthAndYear.replace("-", ".") + "%'" + plusUserString)){

            while (rs.next()) {
                resultSum += Float.parseFloat(rs.getString("sum"));
            }
        } catch (SQLException e) {
            logger.error("SELECT * FROM Documents INNER JOIN Bons ON Documents.id=Bons.belongsToDocument where date like '%" + monthAndYear.replace("-", ".") + "%'", e);
        }
        return resultSum;
    }

    public static Map<Integer, String> getQRItemMap(){
        Map<Integer, String> itemMap = new HashMap<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from QRItems");) {

            while (rs.next()) {
                itemMap.put(rs.getInt("itemNumber"), rs.getString("itemMapped"));
            }
        } catch (SQLException e) {
            logger.error("select * from QRItems", e);
        }
        return itemMap;
    }

    public static void updateQRItem(Integer itemNumer, String itemName){
        executeSQL("UPDATE QRItems Set itemMapped=\"" + itemName +"\" where itemNumber=" + itemNumer);
    }

    public static List<Document> getDocumentsForMonthAndYear(String monthAndYear){
        List<Document> documentList = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Documents WHERE date like '%" + monthAndYear.replace("-", ".") + "%' AND originalFile like '%Bons%'");
        ) {
           documentList = new ArrayList<>();
            while (rs.next()) {
                Document document = new Image(rs.getString("content"), new File(rs.getString("originalFile")), rs.getInt("id"));
                document.setUser(rs.getInt("user"));
                documentList.add(document);
            }
        } catch (SQLException e) {
            logger.error("SELECT * FROM Documents WHERE date like '%" + monthAndYear + "%' AND originalFile like '%Bons%'", e);
        }
        return documentList;
    }

    public static boolean isFilePresent(File newFile){
        int filesSizeOfNewFile = countDocuments("Documents" ,"where sizeOfOriginalFile=" + FileUtils.sizeOf(newFile));

        return filesSizeOfNewFile > 0;
    }

    public static List<Task> getTasksFromDB(Bot bot){
        List<Task> taskList = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from CalendarTasks");) {

            while (rs.next()) {
                String strategyType = rs.getString("strategyType");
                List<User> userList = new ArrayList<>();
                if(rs.getString("user").equals("ALL")){
                    userList.addAll(getAllowedUsersMap().values());
                }else{
                    userList.add(getAllowedUsersMap().get(Integer.parseInt(rs.getString("user"))));
                }

                Task task = new Task(userList, bot, rs.getString("name"));
                TaskStrategy taskStrategy = null;
                switch (strategyType){
                    case "SimpleCalendarOneTimeStrategy":
                        LocalDateTime time = LocalDateTime.of(rs.getInt("year"),rs.getInt("month"),rs.getInt("day"),rs.getInt("hour"),rs.getInt("minute"));
                        taskStrategy = new SimpleCalendarOneTimeStrategy(task, time);
                        task.setTaskStrategy(taskStrategy);
                        break;
                    case "RegularDailyTaskStrategy":
                        taskStrategy = new RegularDailyTaskStrategy(task);
                        break;
                    case "RegularMonthlyTaskStrategy":
                        taskStrategy = new RegularMonthlyTaskStrategy(task, rs.getInt("day"));
                        break;
                    case "RegularYearlyTaskStrategy":
                        taskStrategy = new RegularYearlyTaskStrategy(task, rs.getInt("day"), rs.getInt("month"));
                        break;
                }
                task.setTaskStrategy(taskStrategy);
                taskList.add(task);
            }
        } catch (SQLException e) {
            logger.error("select * from Task", e);
        }
        return taskList;
    }

    public static void insertTaskToDB(Task task){
        DBUtil.executeSQL(task.getInsertDBString());
    }

     public static List<Bon> getBonsfromDB(String specifySQLStringOrNull){

        String addString = specifySQLStringOrNull == null ? "" : specifySQLStringOrNull;
        List<Bon> bonSet = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
                //TODO inner join mit documenten!
            ResultSet rs = statement.executeQuery("select * from Bons " + addString);) {
            while (rs.next()) {
                bonSet.add(new Bon(rs.getInt("belongsToDocument"), rs.getFloat("sum")));
            }
        } catch (SQLException e) {
            logger.error("select * from Bons", e);
        }
        return bonSet;
    }


    public static List<Document> getDocumentsByTag(String tag){
        List<Integer> documentIds = new ArrayList<>();
        List<Document> documentList = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
        ResultSet rs = statement.executeQuery("select belongsToDocument from Tags where Tag like '%" + tag + "%'");) {

            while (rs.next()) {
                documentIds.add(rs.getInt("belongsToDocument"));
            }
        } catch (SQLException e) {
            logger.error("select belongsToDocument from Tags where Tag like '%" + tag + "%'", e);
        }
        for(Integer id : documentIds){
            documentList.addAll(DBUtil
                    .showDocumentsFromSQLExpression("select * from Documents where id=" + id + ""));
        }
        return documentList;
    }

    public static List<Document> showDocumentsFromSQLExpression(String sqlExpression) {
        List<Document> documentList = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
        ResultSet rs = statement.executeQuery(sqlExpression);) {

            documentList = new ArrayList<>();
            while (rs.next()) {
                //TODO auch pdfs eigene klasse schreiben
                Image image = new Image(rs.getString("content"), new File(rs.getString("originalFile")), rs.getInt("id"));
                image.setTagSet(getTagsForDocument(image));
                documentList.add(image);
            }

        } catch (SQLException e) {
            logger.error(sqlExpression, e);
        }
        return documentList;
    }

    public static int countDocuments(String tableName, String sqlAddition){
        int count = 0;
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + tableName + " " + sqlAddition);) {

            while (rs.next()) {
                count = rs.getInt("Count(*)");
            }
        } catch (SQLException e) {
            logger.error("SELECT COUNT(*) FROM " + tableName + " " + sqlAddition, e);
        }
        return count;
    }

    private static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() && dbFile.exists()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            }
        } catch (SQLException e) {
            logger.error("Could not create Connection jdbc:sqlite:" + dbFile.getAbsolutePath(), e);
            System.exit(2);
        }

        return connection;
    }
}
