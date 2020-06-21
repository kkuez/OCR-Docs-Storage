package com.backend;

import com.Main;
import com.backend.taskHandling.TaskFactory;
import com.backend.taskHandling.Task;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.Image;
import com.objectTemplates.User;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

class DBDAO {

    private Logger logger = Main.getLogger();

    private Connection connection = null;

    File dbFile = new File(ObjectHub.getInstance().getProperties().getProperty("dbPath"));

    private Document lastProcessedDoc = null;

    DBDAO() {}

    List<Document> getDocumentsForSearchTerm(String searchTerm) {
        Map<File, Document> documentMap = new HashMap<>();

        showDocumentsFromSQLExpression("select * from Documents where content like '%" + searchTerm + "%'").forEach(document -> {
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

    Document getDocument(int id) {
        return showDocumentsFromSQLExpression("select * from Documents where id =" + id + "").get(0);
    }

    Set<String> getFilePathOfDocsContainedInDB() {
        Set<String> filePathSet = new HashSet<>();
        List<Document> documentList = showDocumentsFromSQLExpression("select * from Documents");
        documentList.forEach(document -> filePathSet.add(document.getOriginFile().getAbsolutePath()));
        return filePathSet;
    }

    void updateDocument(Document document) {
        String divider = ", ";
        StringBuilder updateStatement = new StringBuilder("update Documents set ");
        updateStatement.append("content = '");
        updateStatement.append(document.getContent());
        updateStatement.append("'");
        updateStatement.append(divider);
        updateStatement.append("originalFile = '");
        updateStatement.append(document.getOriginFile().getAbsolutePath());
        updateStatement.append("'");
        updateStatement.append(divider);
        updateStatement.append("date = '");
        updateStatement.append(document.getDate());
        updateStatement.append("'");
        updateStatement.append(divider);
        updateStatement.append("inZipFile = '");
        updateStatement.append(document.getInZipFile());
        updateStatement.append("'");
        updateStatement.append(divider);
        updateStatement.append("sizeOfOriginalFile = ");
        updateStatement.append(document.getOriginFile().length());
        updateStatement.append(" where id = ");
        updateStatement.append(document.getId());
        updateStatement.append(";");

        if(document instanceof Bon) {
            Bon bon = (Bon) document;
            updateStatement.append("update Bons set sum = ");
            updateStatement.append(bon.getSum());
            updateStatement.append(" where belongsToDocument = ");
            updateStatement.append(bon.getId());
        }

        executeSQL(updateStatement.toString());
    }

    Map<Integer, User> getAllowedUsersMap(BackendFacade facade){
        Map<Integer, User> userMap = new HashMap<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from AllowedUsers");) {

            while (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("name"), facade);
                userMap.put(rs.getInt("id"), user);
            }
        } catch (SQLException e) {
            logger.error("select * from AllowedUsers", e);
        }
        return userMap;
    }

    List<String> getShoppingListFromDB(){
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

    void insertShoppingItem(String item) {
        executeSQL("insert into ShoppingList(item) Values ('" + item + "')");
    }

    List<String> getMemos(long userId){
        List<String> memoList = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM Memos where user=" + userId)) {
            while (rs.next()) {
                memoList.add(rs.getString("item"));
            }
        } catch (SQLException e) {
            logger.error("SELECT * FROM Memos where user=" + userId + ")", e);
        }
        return memoList;
    }

    List<String> getStandardListFromDB(){
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

  void insertDocument(Document document){
        if(document.getClass().equals(Image.class)){
            lastProcessedDoc = document;
        }
        executeSQL(document.getInsertDBString(countDocuments("Documents", "")));
    }

    void removeTask(Task task){
        int year = task.getExecutionStrategy().getTime().getYear();
        int month = task.getExecutionStrategy().getTime().getMonth().getValue();
        int day = task.getExecutionStrategy().getTime().getDayOfMonth();
        int hour = task.getExecutionStrategy().getTime().getHour();
        int minute = task.getExecutionStrategy().getTime().getMinute();

        executeSQL("delete from CalendarTasks where name='" + task.getName() + "' AND year=" + year + " AND month=" + month + " AND day=" + day + " AND hour=" + hour + " AND minute=" + minute);
    }

    void removeLastProcressedDocument(){
        executeSQL("delete from Documents where id=" + lastProcessedDoc.getId() + "");
        executeSQL("delete from Bons where belongsToDocument=" + lastProcessedDoc.getId() + "");
        FileUtils.deleteQuietly(lastProcessedDoc.getOriginFile());
    }

    Set<String> getTagsForDocument(Document document){
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

    void executeSQL(String sqlStatement) {
        try(Statement statement = getConnection().createStatement();) {
            statement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            logger.error(sqlStatement, e);
        }
    }

    float getSumMonth(LocalDate monthAndYear, User userOrNull){
        float resultSum = 0f;
        String plusUserString = userOrNull == null ? "" :" AND USER=" + userOrNull.getId();
        String statementString = "SELECT * FROM Documents INNER JOIN Bons ON Documents.id=Bons.belongsToDocument where date like '%" + (monthAndYear.getYear() + "-" + monthAndYear.getMonthValue()).replace("-", ".") + "%'" + plusUserString;
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery(statementString)){
            while (rs.next()) {
                resultSum += Float.parseFloat(rs.getString("sum"));
            }
        } catch (SQLException e) {
            logger.error("Couldnt execute\n" + statementString, e);
        }
        return resultSum;
    }

    Map<Integer, String> getQRItemMap(){
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

    void updateQRItem(Integer itemNumber, String itemName){
        executeSQL("UPDATE QRItems Set itemMapped=\"" + itemName +"\" where itemNumber=" + itemNumber);
    }

    boolean isFilePresent(File newFile){
        int filesSizeOfNewFile = countDocuments("Documents" ,"where sizeOfOriginalFile=" + FileUtils.sizeOf(newFile));

        return filesSizeOfNewFile > 0;
    }

    List<Task> getTasksFromDB(BackendFacade facade){
        List<Task> taskList = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from CalendarTasks");) {
            while (rs.next()) {
                Task task = TaskFactory.getTask(rs, facade);
                taskList.add(task);
            }
        } catch (SQLException e) {
            logger.error("select * from Task", e);
        }
        return taskList;
    }

    void insertTaskToDB(Task task){
        executeSQL(task.getInsertDBString());
    }

    List<Document> getDocumentsByTag(String tag){
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
            documentList.addAll(showDocumentsFromSQLExpression("select * from Documents where id=" + id + ""));
        }
        return documentList;
    }

    List<Document> showDocumentsFromSQLExpression(String sqlExpression) {
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

    int countDocuments(String tableName, String sqlAddition){
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

    private Connection getConnection() {
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

    public List<Bon> getBonsForMonth(int year, int month) {
        //TODO es gibt ein dateTime Format von SQLite
        String monthAndYear = month + "-" + year;
        List<Bon> resultBons = new ArrayList<>();
        try(Statement statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Documents INNER JOIN Bons ON Documents.id=Bons.belongsToDocument where date like '%" + monthAndYear.replace("-", ".") + "%'")){
            while (rs.next()) {
                String content = rs.getString("content");
                String originalFilePath = rs.getString("originalFile");
                int userInt = rs.getInt("user");
                float sum = rs.getFloat("sum");
                int id = rs.getInt("id");

                Document document = new Image(content, new File(originalFilePath), id);
                document.setUser(userInt);
                Bon bon = new Bon(document, sum);
                resultBons.add(bon);
            }
        } catch (SQLException e) {
            logger.error("Couldnt sql to get Bons for time " + month + "-" + year, e);
        }
        return resultBons;
    }

    public void deleteFromShoppingList(String item) {
        executeSQL("delete from ShoppingList where item='" +  item + "'");
    }

    public void insertToStandartList(String item) {
        executeSQL("insert into StandardList(item) Values ('" + item + "')");
    }

    public void deleteFromStandartList(String itemName) {
        executeSQL("delete from StandardList where item='" +  itemName + "'");
    }

    public void insertMemo(String itemName, long userId) {
        executeSQL("insert into Memos(item, user) Values ('" + itemName + "', " + userId + ")");
    }

    public void deleteMemo(String memoName) {
        executeSQL("delete from Memos where item='" +  memoName + "'");
    }

    public void insertTag(int documentId, String tag) {
        executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + documentId + ", '" + tag + "');" );
    }

    public void insertUserToAllowedUsers(Integer id, String firstName, Long chatId) {
        executeSQL("insert into AllowedUsers(id, name, chatId) Values (" + id + ", '" +
                firstName + "', " + chatId + ")");
    }
}
