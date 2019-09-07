package com.Utils;

import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.Image;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                statement.executeUpdate("create table Documents (id INTEGER, content TEXT, originalFile TEXT, date TEXT)");
                statement.close();
                statement.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
                documentList.add(new Image(rs.getString("content"), new File(rs.getString("originalFile"))));
                System.out.println(rs.getString("originalFile"));
            }

            statement.close();
            statement.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documentList;
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
