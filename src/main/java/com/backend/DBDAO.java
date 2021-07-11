package com.backend;

import com.StartUp;
import com.backend.taskhandling.Task;
import com.backend.taskhandling.TaskFactory;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.Image;
import com.objectTemplates.User;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DBDAO {

    private Logger logger = StartUp.createLogger(DBDAO.class);

    private Connection connection = null;

    private static File dbFile = null;
    private Archiver archiver;
    private TaskFactory taskFactory;

    private Document lastProcessedDoc = null;

    @Autowired
    public DBDAO(TaskFactory taskFactory, ObjectHub objectHub, Archiver archiver, BackendFacade facade) {
        dbFile = new File(objectHub.getProperties().getProperty("dbPath"));
        this.taskFactory = taskFactory;
        taskFactory.setAllowedUsersMap(getAllowedUsersMap(facade));
        this.archiver = archiver;
    }

    public static boolean insertNewUser(String name, String password) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("setup.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        dbFile = new File(properties.getProperty("dbPath"));


        try (Connection connectionStatic = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
             PreparedStatement statement = connectionStatic.prepareStatement
                     ("insert into AllowedUsers(name, password) Values (?,?)")) {

            final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            final byte[] hashbytes = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            String sha3Hex = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(hashbytes);

            statement.setString(1, name);
            statement.setString(2, sha3Hex);
            statement.executeUpdate();
        } catch (NoSuchAlgorithmException | SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Float getSum() {
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select SUM(sum) AS Summe from Bons")) {
            while (rs.next()) {
                return rs.getFloat("Summe");
            }
        } catch (SQLException e) {
            logger.error("Cannot calc sum", e);
        }
        return null;

    }


    List<Document> getDocumentsForSearchTerm(String searchTerm) {
        Map<File, Document> documentMap = new HashMap<>();

        showDocumentsFromSQLExpression("select * from Documents where content like '%" + searchTerm + "%'")
                .forEach(document -> {
                    document.setTagSet(getTagsForDocument(document));
                    documentMap.put(document.getOriginFile(), document);
                });

        List<Document> taggedDocuments = getDocumentsByTag(searchTerm);
        taggedDocuments.forEach(document -> documentMap.putIfAbsent(document.getOriginFile(), document));
        List<Document> documentList = new ArrayList<>();
        documentList.addAll(documentMap.values());
        archiver.setDocumentList(documentList);
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
        executeSQL(updateStatement.toString());
    }

    Map<String, User> getAllowedUsersMap(BackendFacade facade) {
        Map<String, User> userMap = new HashMap<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select * from AllowedUsers");) {

            while (rs.next()) {
                User user = new User(rs.getString("name"), facade);
                userMap.put(user.getName(), user);
            }
        } catch (SQLException e) {
            logger.error("select * from AllowedUsers", e);
        }
        return userMap;
    }

    List<String> getShoppingListFromDB() {
        List<String> shoppingList = new ArrayList<>();
        try (Statement statement = getConnection().createStatement();
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

    List<String> getMemos(long userId) {
        List<String> memoList = new ArrayList<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM Memos where user=" + userId)) {
            while (rs.next()) {
                memoList.add(rs.getString("item"));
            }
        } catch (SQLException e) {
            logger.error("SELECT * FROM Memos where user=" + userId + ")", e);
        }
        return memoList;
    }

    List<String> getStandardListFromDB() {
        List<String> standardList = new ArrayList<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM StandardList");) {

            while (rs.next()) {
                standardList.add(rs.getString("item"));
            }
        } catch (SQLException e) {
            logger.error("SELECT * FROM StandardList", e);
        }
        return standardList;
    }

    void insertDocument(Document document) {
        if (!(document instanceof Bon)) {
            lastProcessedDoc = document;
        }
        executeSQL(document.getInsertDBString(document.getId()));
    }

    void deleteTask(Task task) {
        int year = task.getExecutionStrategy().getTime().getYear();
        int month = task.getExecutionStrategy().getTime().getMonth().getValue();
        int day = task.getExecutionStrategy().getTime().getDayOfMonth();
        int hour = task.getExecutionStrategy().getTime().getHour();
        int minute = task.getExecutionStrategy().getTime().getMinute();

        executeSQL("delete from CalendarTasks where name='" + task.getName() + "' AND year=" + year + " AND month="
                + month + " AND day=" + day + " AND hour=" + hour + " AND minute=" + minute);
    }

    void deleteLastProcressedDocument() {
        executeSQL("delete from Documents where id=" + lastProcessedDoc.getId() + "");
        executeSQL("delete from Bons where belongsToDocument=" + lastProcessedDoc.getId() + "");
        FileUtils.deleteQuietly(lastProcessedDoc.getOriginFile());
    }

    Set<String> getTagsForDocument(Document document) {
        Set<String> tagSet = new HashSet<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement
                     .executeQuery("SELECT Tag FROM Tags where belongsToDocument=" + document.getId());) {

            while (rs.next()) {
                tagSet.add(rs.getString("Tag"));
            }
        } catch (SQLException e) {
            logger.error("SELECT Tag FROM Tags where belongsToDocument=" + document.getId(), e);
        }
        return tagSet;
    }

    void executeSQL(String sqlStatement) {
        try (Statement statement = getConnection().createStatement();) {
            statement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            logger.error(sqlStatement, e);
        }
    }

    float getSumMonth(LocalDate monthAndYear, User userOrNull) {
        float resultSum = 0f;
        String plusUserString = userOrNull == null ? "" : " AND USER=" + userOrNull.getName();
        String statementString = "SELECT * FROM Documents INNER JOIN Bons ON Documents.id=Bons.belongsToDocument where date like '%"
                + (monthAndYear.getMonthValue() + "-" + monthAndYear.getYear()).replace("-", ".") + "%'"
                + plusUserString;
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(statementString)) {
            while (rs.next()) {
                resultSum += Float.parseFloat(rs.getString("sum"));
            }
        } catch (SQLException e) {
            logger.error("Couldnt execute\n" + statementString, e);
        }
        return resultSum;
    }

    void updateQRItem(Integer itemNumber, String itemName) {
        executeSQL("UPDATE QRItems Set itemMapped=\"" + itemName + "\" where itemNumber=" + itemNumber);
    }

    boolean isFilePresent(File newFile) {
        int filesSizeOfNewFile = countDocuments("Documents", "where sizeOfOriginalFile=" + FileUtils.sizeOf(newFile));

        return filesSizeOfNewFile > 0;
    }

    List<Task> getTasksFromDB(BackendFacade facade) {
        List<Task> taskList = new ArrayList<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select * from CalendarTasks");) {
            while (rs.next()) {
                Task task = taskFactory.getTask(rs, facade);
                taskList.add(task);
            }
        } catch (SQLException e) {
            logger.error("select * from Task", e);
        }
        return taskList;
    }

    void insertTaskToDB(Task task) {
        executeSQL(task.getInsertDBString());
    }

    List<Document> getDocumentsByTag(String tag) {
        List<Integer> documentIds = new ArrayList<>();
        List<Document> documentList = new ArrayList<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement
                     .executeQuery("select belongsToDocument from Tags where Tag like '%" + tag + "%'");) {

            while (rs.next()) {
                documentIds.add(rs.getInt("belongsToDocument"));
            }
        } catch (SQLException e) {
            logger.error("select belongsToDocument from Tags where Tag like '%" + tag + "%'", e);
        }
        for (Integer id : documentIds) {
            documentList.addAll(showDocumentsFromSQLExpression("select * from Documents where id=" + id + ""));
        }
        return documentList;
    }

    List<Document> showDocumentsFromSQLExpression(String sqlExpression) {
        List<Document> documentList = new ArrayList<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sqlExpression);) {

            documentList = new ArrayList<>();
            while (rs.next()) {
                Image image = new Image(rs.getString("content"), new File(rs.getString("originalFile")),
                        rs.getInt("id"), rs.getString("user"));
                image.setTagSet(getTagsForDocument(image));
                documentList.add(image);
            }

        } catch (SQLException e) {
            logger.error(sqlExpression, e);
        }
        return documentList;
    }

    int countDocuments(String tableName, String sqlAddition) {
        int count = 0;
        try (Statement statement = getConnection().createStatement();
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

    List<Bon> getAllBons() {
        // TODO es gibt ein dateTime Format von SQLite
        List<Bon> resultBons = new ArrayList<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT * FROM Documents INNER JOIN Bons ON Documents.id=Bons.belongsToDocument")) {
            while (rs.next()) {
                String content = rs.getString("content");
                String originalFilePath = rs.getString("originalFile");
                String userName = rs.getString("user");
                float sum = rs.getFloat("sum");
                int id = rs.getInt("id");
                String date = rs.getString("date");

                Document document = new Image(content, new File(originalFilePath), id, userName);
                document.setDate(date);
                Bon bon = new Bon(document, sum);
                resultBons.add(bon);
            }
        } catch (SQLException e) {
            logger.error("Couldnt sql to get Bons", e);
        }
        return resultBons;
    }

    public List<Bon> getBonsForMonth(int year, int month) {
        // TODO es gibt ein dateTime Format von SQLite
        String monthAndYear = month + "-" + year;
        List<Bon> resultBons = new ArrayList<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT * FROM Documents INNER JOIN Bons ON Documents.id=Bons.belongsToDocument where date like '%"
                             + monthAndYear.replace("-", ".") + "%'")) {
            while (rs.next()) {
                String content = rs.getString("content");
                String originalFilePath = rs.getString("originalFile");
                String userName = rs.getString("user");
                float sum = rs.getFloat("sum");
                int id = rs.getInt("id");

                Document document = new Image(content, new File(originalFilePath), id, userName);
                Bon bon = new Bon(document, sum);
                resultBons.add(bon);
            }
        } catch (SQLException e) {
            logger.error("Couldnt sql to get Bons for time " + month + "-" + year, e);
        }
        return resultBons;
    }

    public void deleteFromShoppingList(String item) {
        executeSQL("delete from ShoppingList where item='" + item + "'");
    }

    public void insertToStandartList(String item) {
        executeSQL("insert into StandardList(item) Values ('" + item + "')");
    }

    public void deleteFromStandartList(String itemName) {
        executeSQL("delete from StandardList where item='" + itemName + "'");
    }

    public void insertMemo(String itemName, long userId) {
        executeSQL("insert into Memos(item, user) Values ('" + itemName + "', " + userId + ")");
    }

    public void deleteMemo(String memoName) {
        executeSQL("delete from Memos where item='" + memoName + "'");
    }

    public void insertTag(int documentId, String tag) {
        executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + documentId + ", '" + tag + "');");
    }

    public void insertUserToAllowedUsers(Integer id, String firstName, Long chatId) {
        executeSQL(
                "insert into AllowedUsers(id, name, chatId, hasXORKey) Values (" + id + ", '" + firstName
                        + "', " + chatId + ", 0)");
    }

    public void deleteTask(UUID uuid) {
        executeSQL("delete from CalendarTasks Where eID='" + uuid + "'");
    }

    public List<Task> getTasksFromDB(BackendFacadeImpl backendFacade, String userid) {
        List<Task> taskList = new ArrayList<>();
        final LocalDateTime now = LocalDateTime.now();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select * from CalendarTasks where user='" + userid + "' OR " +
                     "user='ALL'");) {
            while (rs.next()) {
                Task task = taskFactory.getTask(rs, backendFacade);
                if (task.getExecutionStrategy().getTime().isBefore(now)) {
                    deleteTask(task.geteID());
                } else {
                    taskList.add(task);
                }
            }
        } catch (SQLException e) {
            logger.error("select * from Task", e);
        }
        return taskList;
    }

    public Float getSum(String userid) {
        String sqlString = "select SUM(sum) AS Summe from Bons b, Documents d where " +
                "b.belongsToDocument = d.id" + (userid.equals("") ? "" : " AND d.user = '" + userid + "'");
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sqlString)) {
            while (rs.next()) {
                return rs.getFloat("Summe");
            }
        } catch (SQLException e) {
            logger.error("Cannot calc sum", e);
        }
        return 0F;
    }

    public void insertBon(Bon bon) {
        executeSQL(bon.getInsertDBString(0));
    }

    public String getXORKey() {
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select key from Settings")) {
            while (rs.next()) {
                return rs.getString("key");
            }
        } catch (SQLException e) {
            logger.error("Cannot get XORKey", e);
        }
        return null;
    }

    public void setXORKey(String key) {
        executeSQL("UPDATE Settings Set key='" + key + "',lastRenewalDate='" + LocalDate.now() + "'");
    }

    public LocalDate getLastKeyRenewalDate() {
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select lastRenewalDate from Settings")) {
            while (rs.next()) {
                return LocalDate.parse(rs.getString("lastRenewalDate"));
            }
        } catch (SQLException e) {
            logger.error("Cannot get lastRenewalDate", e);
        }
        return null;
    }

    public void setUserHasXORKey(Integer userID, boolean has) {
        executeSQL("UPDATE AllowedUsers Set hasXORKey=" + (has ? "1" : "0") + " where id=" + userID);
    }

    public boolean hasXORKey(Integer userID) {
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select hasXORKey from AllowedUsers where id=" + userID)) {
            while (rs.next()) {
                return rs.getInt("hasXORKey") == 1;
            }
        } catch (SQLException e) {
            logger.error("Cannot get lastRenewalDate", e);
        }
        return false;
    }

    public boolean checkCredentials(String userid, String passw) {
        final MessageDigest digest;
        String sha3Hex = "";
        try {
            digest = MessageDigest.getInstance("SHA3-256");
            final byte[] hashbytes = digest.digest(
                    passw.getBytes(StandardCharsets.UTF_8));
            sha3Hex = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(hashbytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha3Hex.equals(getPassWHashForUser(userid));
    }

    private String getPassWHashForUser(String name) {
        String password = "";
        try (PreparedStatement statement =
                     getConnection().prepareStatement("select password from AllowedUsers where name=?");) {
            statement.setString(1, name);
            final ResultSet resultSet = statement.executeQuery();
            password = resultSet.getString("password");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return password;
    }

    public List<Float> getLastSums(String userid, Integer lastMany) {
        List<Float> sums = new ArrayList<>(lastMany);
        String sqlString = "select b.sum from Bons b, Documents d where " +
                "b.belongsToDocument = d.id" + (userid.equals("") ? "" : " AND d.user = '" + userid + "' " +
                "order by d.id desc");
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sqlString)) {
            int i = 0;
            while (rs.next() && i < lastMany) {
                sums.add(rs.getFloat("sum"));
                i++;
            }
        } catch (SQLException e) {
            logger.error("Cannot get sums for user " + userid, e);
        }
        return sums;
    }

    public void deleteBon(String userid, float sum) {
        //get Document id
        String sqlString = "select d.id, d.originalFile from Bons b, Documents d where b.belongsToDocument = d.id " +
                "AND sum=" + sum;
        int id = 99999;
        String filePath = "";
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sqlString)) {
            int i = 0;
            if(rs.next()) {
                id = rs.getInt("id");
                filePath = rs.getString("originalFile");
            }
        } catch (SQLException e) {
            logger.error("Cannot get sums for user " + userid, e);
        }

        executeSQL("delete from Bons where belongsToDocument=" + id);
        executeSQL("delete from Documents where id=" + id);
        //FIXME sollte woanders deleten
        if(!filePath.equals("")) {
            try {
                FileUtils.forceDelete(new File(filePath));
            } catch (IOException e) {
                logger.error("Cant delete bon " + filePath, e);
            }
        }
    }
}
