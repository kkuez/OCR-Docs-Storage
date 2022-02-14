package com.backend;

import com.backend.taskhandling.Task;
import com.backend.taskhandling.TaskFactory;
import com.backend.taskhandling.strategies.ExecutionStrategy;
import com.backend.taskhandling.strategies.RegularExecutionStrategy;
import com.backend.taskhandling.strategies.StrategyType;
import com.data.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(DBDAO.class);

    private Connection connection = null;

    private static File dbFile = null;
    private final TaskFactory taskFactory;

    @Autowired
    public DBDAO(TaskFactory taskFactory, ObjectHub objectHub, BackendFacade facade) {
        dbFile = new File(objectHub.getProperties().getProperty("dbPath"));
        this.taskFactory = taskFactory;
        taskFactory.setAllowedUsersMap(getAllowedUsersMap(facade));
        setBonUUIDs();
    }

    public static boolean insertNewUser(String name, String password) {
        Properties properties = new Properties();
        try(final FileInputStream setupPropertiesInputStream = new FileInputStream("setup.properties")) {
            properties.load(setupPropertiesInputStream);
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

    Map<String, User> getAllowedUsersMap(BackendFacade facade) {
        Map<String, User> userMap = new HashMap<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select * from AllowedUsers")) {

            while (rs.next()) {
                User user = new User(rs.getString("name"));
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
             ResultSet rs = statement.executeQuery("SELECT * FROM ShoppingList")) {

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

    List<Memo> getMemos(User user) {
        // Get Memos
        Map<Integer, Memo> memoMap = new HashMap<>();
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select * from Memos m " +
                     "inner Join Users_Memos um on m.id = um.memoid")){

            while(rs.next()) {
                final int memoId = rs.getInt("id");
                if(!memoMap.containsKey(memoId)) {
                    memoMap.put(memoId, new Memo(memoId,  rs.getString("memoText"),
                            LocalDateTime.parse(rs.getString("dateTime"))));
                }
                memoMap.get(memoId).addUserName(rs.getString("username"));
            }
        } catch (SQLException e) {
            logger.error("Could not get Memos for " + user.getName(), e);
        }

        final Collection<Memo> values = memoMap.values();
        List<Memo> memoList = new ArrayList<>(values);
        for(Memo memo: values) {
            if(!memo.getUserNames().contains(user.getName())) {
                memoList.remove(memo);
            }
        }

        return memoList;
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

    void executeSQL(String sqlStatement) {
        try (Statement statement = getConnection().createStatement()) {
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

    void insertTaskToDB(Task task) {
        executeSQL(task.getInsertDBString());
    }

    int countDocuments() {
        int count = 0;
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM Documents")) {

            while (rs.next()) {
                count = rs.getInt("Count(*)");
            }
        } catch (SQLException e) {
            logger.error("SELECT COUNT(*) FROM Documents", e);
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

    public void deleteFromShoppingList(String item) {
        executeSQL("delete from ShoppingList where item='" + item + "'");
    }

    public void deleteTask(UUID uuid) {
        executeSQL("delete from CalendarTasks Where eID='" + uuid + "'");
    }

    List<Task> getTasksFromDB(BackendFacade facade) {
        return getTasksFromDB(facade, Optional.empty());
    }

    public List<Task> getTasksFromDB(BackendFacade backendFacade, Optional<String> userid) {
        List<Task> taskList = new ArrayList<>();
        final LocalDateTime now = LocalDateTime.now();
        String statementString = "select * from CalendarTasks";
        if(userid.isPresent()) {
            statementString += " where user='" + userid.get() + "' OR " + "user='ALL'";
        }

        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(statementString)) {
            while (rs.next()) {
                Task task = taskFactory.getTask(rs, backendFacade);
                if (!(task.getExecutionStrategy() instanceof RegularExecutionStrategy)
                        && task.getExecutionStrategy().getTime().toLocalDate().isBefore(now.toLocalDate())) {
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
                     getConnection().prepareStatement("select password from AllowedUsers where name=?")) {
            statement.setString(1, name);
            final ResultSet resultSet = statement.executeQuery();
            password = resultSet.getString("password");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return password;
    }

    public List<Bon> getLastBons(String userid, Integer lastMany) {
        List<Bon> bons = new ArrayList<>(lastMany);
        String sqlString = "select * from Bons b, Documents d where " +
                "b.belongsToDocument = d.id" + (userid.equals("") ? "" : " AND d.user = '" + userid + "' " +
                "order by d.id desc");
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sqlString)) {
            int i = 0;
            while (rs.next() && i < lastMany) {
                final UUID uid = UUID.fromString(rs.getString("uid"));
                final String date = rs.getString("date");
                Bon bon = new Bon(rs.getFloat("sum"), uid, date);
                bons.add(bon);
                i++;
            }
        } catch (SQLException e) {
            logger.error("Cannot get sums for user " + userid, e);
        }
        return bons;
    }

    public void deleteBon(String userid, UUID uid) {
        //get Document id
        String sqlString = "select d.id, d.originalFile from Bons b, Documents d where b.belongsToDocument = d.id " +
                "AND uid='" + uid + "'";
        int id = 99999;
        String filePath = "";
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sqlString)) {
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

    public void insertMemo(Memo memo) {
        executeSQL("insert into Memos(memoText, dateTime) Values ('" + memo.getMemoText() + "', '" +
        memo.getFromTime().toString() + "')");

        String sqlString = "select id from Memos where memoText='" + memo.getMemoText() + "'";
        int memoId = 99999;
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sqlString)) {
            while (rs.next()) {
                memoId = rs.getInt("id");
            }
        } catch (SQLException e) {
            logger.error("Could not enter memo + " + memo.getMemoText(), e);
            return;
        }

        for(String username: memo.getUserNames()) {
            executeSQL("insert into Users_Memos(username, memoid) VALUES('" + username + "'," +
                    memoId + ")");
        }
    }

    private void setBonUUIDs() {
        for (int i = 1; i < 300; i++) {
            try {
                executeSQL("update Bons set uid='" + UUID.randomUUID() + "' where belongsToDocument=" + i);
            } catch (Exception e) {
            }
        }
    }

    public void shift(Task task) {
        shift(task, 1);
    }

    public void shift(Task task, long times) {
        ExecutionStrategy executionStrategy = task.getExecutionStrategy();

        if (executionStrategy instanceof RegularExecutionStrategy) {
            StrategyType executionStrategyType = executionStrategy.getType();
            LocalDateTime time = task.getExecutionStrategy().getTime();

            LocalDateTime timeToShift;
            switch (executionStrategyType) {
                case DAILY:
                    timeToShift = time.plusDays(times);
                    break;
                case WEEKLY:
                    timeToShift = time.plusWeeks(times);
                    break;
                case MONTHLY:
                    timeToShift = time.plusMonths(times);
                    break;
                case YEARLY:
                    timeToShift = time.plusYears(times);
                    break;
                case MINUTELY:
                    //TODO Why would anyone wish for a minutely reminder :D
                    //TODO for Checkconnection Task dooooooh
                default:
                    return;
            }

            executeSQL("update CalendarTasks set eID='" + UUID.randomUUID() + "'," +
                    "time='" + timeToShift.withNano(0) + "' where eID='" + task.geteID() + "'");
        }
    }

    public void deleteMemos(List<Memo> memos, List<User> users) {
        memos.forEach(memo -> deleteMemo(memo, users));
    }

    public void deleteMemo(Memo memo, List<User> users) {
        for (User user : users) {
            executeSQL("delete from Users_Memos where username='" + user.getName()
                    + "' and memoid=" + memo.getId());
        }

        executeSQL("delete from Memos where id=" + memo.getId());
    }
}
