package com.Utils;

import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.Image;
import com.ObjectTemplates.User;

import javax.print.Doc;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class DBUtil {

    private static Connection connection = null;

    static File dbFile = new File(ObjectHub.getInstance().getProperties().getProperty("dbPath"));

    public static List<Document> getFilesForSearchTerm(String searchTerm) {
        List<Document> documentList = DBUtil
                .showResultsFromSQLExpression("select * from Documents where content like '%" + searchTerm + "%'");

        ObjectHub.getInstance().getArchiver().setDocumentList(documentList);
        return documentList;
    }

    public static Set<String> getFilePathOfDocsContainedInDB() {
        Set<String> filePathSet = new HashSet<>();
        List<Document> documentList = showResultsFromSQLExpression("select * from Documents");
        documentList.forEach(document -> filePathSet.add(document.getOriginFile().getAbsolutePath()));
        return filePathSet;
    }

    public static void prepareDB() {
        if (dbFile.exists()) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath().replace("\\", "/"));
                Statement statement = connection.createStatement();
                statement.executeUpdate("");
                statement.close();
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
            statement.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userMap;
    }

    public static void insertDocumentToDB(Document document){
        DBUtil.executeSQL(document.getInsertDBString());
    }

    public static void executeSQL(String sqlStatement) {
        try {
            Statement statement = getConnection().createStatement();

            statement.executeUpdate(sqlStatement);
            statement.close();
            statement.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Document> showResultsFromSQLExpression(String sqlExpression) {
        Statement statement = null;
        List<Document> documentList = new ArrayList<>();
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery(sqlExpression);
            documentList = new ArrayList<>();
            while (rs.next()) {
                documentList.add(new Image(rs.getString("content"), new File(rs.getString("originalFile")), rs.getInt("id")));
                System.out.println(rs.getString("originalFile"));
            }

            statement.close();
            statement.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documentList;
    }

    public static int countDocuments(){
        Statement statement = null;
        int count = 0;
        try {
            statement = getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM Documents");
            while (rs.next()) {
                count = rs.getInt("Count(*)");
            }

            statement.close();
            statement.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() && dbFile.exists()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }
}
