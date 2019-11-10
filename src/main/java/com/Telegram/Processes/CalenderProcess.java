package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.Misc.TaskHandling.Strategies.SimpleCalendarOneTimeStrategy;
import com.Misc.TaskHandling.Task;
import com.ObjectHub;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import com.Utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalenderProcess extends Process {

    private int year;

    private int month;

    private int day;

    private int hour;

    private int minute;

    private Task task;

    private String type;

    public CalenderProcess(ProgressReporter reporter, Bot bot, Update update, Map<Integer, User> allowedUsersMap) {
        super(reporter);
        setBot(bot);
        try {
            performNextStep("Termin hinzufügen", update, allowedUsersMap);
        } catch (TelegramApiException e) {
            if(((TelegramApiException) e).getCause().getLocalizedMessage().contains("message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message")){
                LogUtil.log("Message not edited, no need.");
            }else{
                LogUtil.logError(((TelegramApiException) e).getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) throws TelegramApiException{
        User user = getBot().getNonBotUserFromUpdate(update);
        String[] commandValue = deserializeInput(update);
        Message message = null;
        try {
            switch (commandValue[0]) {
                case "done":
                    getBot().sendMsg("Ok :)", update, null, false, false);
                    close();
                    break;
                case "chooseStrategy":
                    task = new Task(user, getBot());
                    switch (commandValue[1]) {
                        case "oneTime":
                            type = "oneTime";
                            try {
                                getBot().sendAnswerCallbackQuery("Bezeichnung wählen", false, update.getCallbackQuery());
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            message = getBot().simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
                            break;
                        case "regular":
                            type = "regular";
                            //TODO hinzufügen!!
                            //task.setTaskStrategy(new RegularTaskStrategy(task));
                            try {
                                getBot().sendAnswerCallbackQuery("Art wählen", false, update.getCallbackQuery());
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            message = getBot().simpleEditMessage("Einheit?", update, KeyboardFactory.KeyBoardType.Calendar_Regular_Choose_Unit);
                            break;
                    }
                    break;
                case "daily":
                    type = "regularDaily";
                    break;
                case "weekly":
                    type = "regularWeekly";//TODO ausfüllen
                    break;
                case "monthly":
                    type = "regularMonthly";
                    break;
                case "yearly":
                    type = "regularYearly";
                    break;
                case "chooseYear":
                    year = Integer.parseInt(commandValue[1]);
                    try {
                        getBot().sendAnswerCallbackQuery(year + " gewählt.", false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    message = getBot().simpleEditMessage("Welche Monat?", update, KeyboardFactory.KeyBoardType.Calendar_Month, "chooseMonth");
                    break;
                case "chooseMonth":
                    month = Integer.parseInt(TimeUtil.getMonthMapStringKeys().get(commandValue[1]));
                    try {
                        getBot().sendAnswerCallbackQuery(month + " gewählt.", false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    message = getBot().simpleEditMessage("Welcher Tag?", getBot().getMassageFromUpdate(update), KeyboardFactory.createInlineKeyboardForYearMonth(year, month), "chooseDay");
                    break;
                case "day":
                    day = Integer.parseInt(commandValue[1]);
                    try {
                        getBot().sendAnswerCallbackQuery(day + " gewählt.", false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    if(type.equals("oneTime")) {
                        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, 4, 0);
                        task.setTaskStrategy(new SimpleCalendarOneTimeStrategy(task, localDateTime));
                    }else{
                        if(type.startsWith("regular")){

                        }
                    }
                    message = getBot().simpleEditMessage("Für wen?", update, KeyboardFactory.KeyBoardType.User_Choose, "chooseUser");
                    break;
                case "forMe":
                    task.getUserList().add(user);
                    DBUtil.executeSQL(task.getInsertDBString());
                    getSentMessages().add(message);
                    ObjectHub.getInstance().getTasksRunnable().getTasksToDo().add(task);
                    close();
                    getBot().simpleEditMessage("Termin eingetragen :)", update, KeyboardFactory.KeyBoardType.NoButtons);
                    break;
                case "forAll":
                    allowedUsersMap.values().forEach(user1 -> task.getUserList().add(user1));
                    DBUtil.executeSQL(task.getInsertDBString());
                    getSentMessages().add(message);
                    ObjectHub.getInstance().getTasksRunnable().getTasksToDo().add(task);
                    close();
                    getBot().simpleEditMessage("Termin eingetragen :)", update, KeyboardFactory.KeyBoardType.NoButtons);
                    break;
                case "Termine anzeige":
                    StringBuilder messageOfTasks = new StringBuilder();
                    List<Task> taskList = DBUtil.getTasksFromDB(getBot());
                    for (Task task : taskList) {
                        messageOfTasks.append("\n" + task.getTaskStrategy().getTime().toString().replace("T", " um ") + " Uhr:\n");
                        messageOfTasks.append(task.getName() + "\n");
                        task.getUserList().forEach(user1 -> messageOfTasks.append(", " + user1.getName()));
                    }
                    String messageString = messageOfTasks.toString().replaceFirst(", ", "").replaceFirst("\n", "");
                    getBot().sendMsg(messageString, update, KeyboardFactory.KeyBoardType.NoButtons, true, false);
                    close();
                    break;
                case "Termin hinzufügen":
                    getBot().sendMsg("Art des Termins wählen:", update, KeyboardFactory.KeyBoardType.Calendar_Choose_Strategy, "chooseStrategy", true, true);
                    break;
                case "Termin löschen":
                    List<String> taskNames = new ArrayList<>();
                    DBUtil.getTasksFromDB(getBot()).forEach(task1 -> taskNames.add(task1.getName()));
                    ReplyKeyboard listKeyboard = KeyboardFactory.getInlineKeyboardForList(taskNames, "deleteTask");
                    message = getBot().sendKeyboard("Welchen Termin willst du löschen?", update, listKeyboard, false);
                    break;
                case "deleteTask":
                    Task taskToRemove = null;
                    for (Task task : DBUtil.getTasksFromDB(getBot())) {
                        if (task.getName().equals(commandValue[1])) {
                            taskToRemove = task;
                            break;
                        }
                    }
                    if (taskToRemove != null) {
                        DBUtil.removeTask(taskToRemove);
                        try {
                            getBot().sendAnswerCallbackQuery(taskToRemove.getName() + " gelöscht :)", false, update.getCallbackQuery());
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        List<String> taskNames1 = new ArrayList<>();
                        DBUtil.getTasksFromDB(getBot()).forEach(task1 -> taskNames1.add(task1.getName()));
                        ReplyKeyboard listKeyboard1 = KeyboardFactory.getInlineKeyboardForList(taskNames1, "deleteTask");
                        message = getBot().simpleEditMessage("Welchen Termin willst du löschen?", getBot().getMassageFromUpdate(update), listKeyboard1, "deleteTask");
                    }
                    break;
                default:
                    task.setName(commandValue[0]);
                    message = getBot().sendMsg("Welches Jahr?", update, KeyboardFactory.KeyBoardType.Calendar_Year, "chooseYear", false, true);
                    break;
            }
        }catch (TelegramApiException e) {
            if(e.getMessage().equals("Error editing message reply markup")){
                LogUtil.log("1 message not changed.");
            }else{
                LogUtil.logError(((TelegramApiRequestException) e).getApiResponse(), e);
            }
        }
        getBot().getNonBotUserFromUpdate(update).setBusy(false);
        if(message != null){
            getSentMessages().add(message);
        }
    }

    @Override
    public String getProcessName() {
        return "Calender Process";
    }

    @Override
    public String getCommandIfPossible(Update update) {
        String inputString = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : update.getMessage().getText();
        if (inputString.startsWith("Termin")) {
            return inputString;
        } else {
            if (inputString.startsWith("deleteTask")) {
                return "deleteTask";
            } else {
                if (inputString.startsWith("chooseName")) {
                    return "chooseName";
                } else {
                    if (inputString.startsWith("chooseStrategy")) {
                        return "chooseStrategy";
                    } else {
                        if (inputString.startsWith("chooseYear")) {
                            return "chooseYear";
                        } else {
                            if (inputString.startsWith("chooseMonth")) {
                                return "chooseMonth";
                            } else {
                                if (inputString.startsWith("day")) {
                                    return "day";
                                } else {
                                    if (inputString.startsWith("forMe")) {
                                        return "forMe";
                                    } else {
                                        if (inputString.startsWith("forAll")) {
                                            {
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return inputString;
        }
    }
}
