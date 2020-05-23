package com.telegram.processes;

import com.controller.reporter.ProgressReporter;
import com.misc.taskHandling.strategies.*;
import com.misc.taskHandling.Task;
import com.ObjectHub;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.telegram.KeyboardFactory;
import com.utils.DBUtil;

import com.utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CalenderProcess extends Process {

    private int year;

    private int month;

    private int day = 0;

    private int hour = 4;

    private int minute = 0;

    private Task task;

    private String type;

    public CalenderProcess(ProgressReporter reporter, Bot bot, Update update, Map<Integer, User> allowedUsersMap) {
        super(reporter);
        setBot(bot);
        try {
            performNextStep("Termin hinzufügen", update, allowedUsersMap);
        } catch (TelegramApiException e) {
            if(((TelegramApiException) e).getCause().getLocalizedMessage().contains("message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message")){
                logger.info("Message not edited, no need.");
            }else{
                logger.error(((TelegramApiException) e).getLocalizedMessage(), e);
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
                    processDone(update);
                    break;
                case "chooseStrategy":
                    message = processChooseStrategy(update, commandValue);
                    break;
                case "daily":
                    message = processDayly(update);
                    break;
                case "monthly":
                    message = processMonthly(update);
                    break;
                case "yearly":
                    message = processYearly(update);
                    break;
                case "chooseYear":
                    message = processChooseYear(update, commandValue);
                    break;
                case "chooseMonth":
                    message = processChooseMonth(update, commandValue);
                    break;
                case "chooseDay":
                    message = processChooseDay(update, commandValue);
                    break;
                case "chooseHour":
                    message = processChooseHour(update, commandValue);
                    break;
                 case "chooseMinute":
                     message = processChooseMinute(update, commandValue);
                    break;
                case "forMe":
                    processForMe(update, user, message);
                    break;
                case "forAll":
                    processForAll(update, allowedUsersMap, message);
                    break;
                case "Termine anzeige":
                    processShowAppointments(update, user);
                    break;
                case "Termin hinzufügen":
                    message = processAddAppointment(update);
                    break;
                case "Termin löschen":
                    message = processDeleteAppointment(update);
                    break;
                case "deleteTask":
                   message = processDeleteTask(update, commandValue);
                    break;
                case "-": //In case a faulty day was chosen by user
                    getBot().sendAnswerCallbackQuery("Ungültiger Tag", false, update.getCallbackQuery());
                    break;
                default:
                    task.setName(commandValue[0]);
                    switch (type){
                        case "oneTime":
                        case "oneTimeWithTime":
                            message = processOneTime(update);
                            break;
                        case "regularDaily":
                            message = askForWhom(update);
                            break;
                        case "regularMonthly":
                            message = processRegularMonthly(update);
                            break;
                        case "regularYearly":
                            message = processRegularYearly(update);
                            break;
                    }
                    break;
            }
        }catch (TelegramApiException e) {
            if(e.getMessage().equals("Error editing message reply markup")){
                logger.info("1 message not changed.");
            }else{
                logger.error(((TelegramApiRequestException) e).getApiResponse(), e);
            }
        }
        getBot().getNonBotUserFromUpdate(update).setBusy(false);
        if(message != null){
            getSentMessages().add(message);
        }
    }

    private void processDone(Update update) {
        getBot().sendMsg("Ok :)", update, null, false, false);
        close();
    }

    private Message processChooseStrategy(Update update, String[] commandValue) throws TelegramApiException {
        Message message = null;
        task = new Task(getBot());
        switch (commandValue[1]) {
            case "oneTime":
                type = "oneTime";
                try {
                    getBot().sendAnswerCallbackQuery("Bezeichnung wählen", false, update.getCallbackQuery());
                } catch (TelegramApiException e) {
                    logger.error("Failed activating bot", e);
                }
                message = getBot().simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
                break;
            case "oneTimeWithTime":
                type = "oneTimeWithTime";
                try {
                    getBot().sendAnswerCallbackQuery("Bezeichnung wählen", false, update.getCallbackQuery());
                } catch (TelegramApiException e) {
                    logger.error("Failed activating bot", e);
                }
                message = getBot().simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
                break;
            case "regular":
                type = "regular";
                try {
                    getBot().sendAnswerCallbackQuery("Wann?", false, update.getCallbackQuery());
                } catch (TelegramApiException e) {
                    logger.error("Failed activating bot", e);
                }
                message = getBot().simpleEditMessage("Wann?", update, KeyboardFactory.KeyBoardType.Calendar_Regular_Choose_Unit);
                break;
        }
        return message;
    }

    private Message processDayly(Update update) throws TelegramApiException {
        type = "regularDaily";
        try {
            getBot().sendAnswerCallbackQuery("Täglich gewählt.", false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return getBot().simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
    }

    private Message processMonthly(Update update) throws TelegramApiException {
        type = "regularMonthly";
        try {
            getBot().sendAnswerCallbackQuery("Monatlich gewählt.", false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return getBot().simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
    }

    private Message processYearly(Update update) throws TelegramApiException {
        type = "regularYearly";
        try {
            getBot().sendAnswerCallbackQuery("Jährlich gewählt.", false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return getBot().simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
    }

    private Message processChooseYear(Update update, String[] commandValue) throws TelegramApiException {
        year = Integer.parseInt(commandValue[1]);
        String question = "Welcher Monat?";
        try {
            getBot().sendAnswerCallbackQuery(year + " gewählt. " + question, false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return getBot().simpleEditMessage(question, update, KeyboardFactory.KeyBoardType.Calendar_Month, "chooseMonth");
    }

    private Message processChooseMonth(Update update, String[] commandValue) throws TelegramApiException {
        month = Integer.parseInt(TimeUtil.getMonthMapStringKeys().get(commandValue[1]));
        String question = "Welcher Tag?";
        try {
            getBot().sendAnswerCallbackQuery(month + " gewählt. " + question, false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return getBot().simpleEditMessage(question, getBot().getMassageFromUpdate(update), KeyboardFactory.createInlineKeyboardForYearMonth(year, month), "chooseDay");
    }

    private Message processChooseDay(Update update, String[] commandValue) throws TelegramApiException {
        Message message = null;
        String question = "Zu welcher Stunde?";
        if(commandValue[1].equals("-")) {
            getBot().sendAnswerCallbackQuery("Ungültiger Tag", false, update.getCallbackQuery());
            return null;
        }
        day = Integer.parseInt(commandValue[1]);
        if(type.equals("oneTimeWithTime")){
            try {
                getBot().sendAnswerCallbackQuery(day + " gewählt. " + question, false, update.getCallbackQuery());
            } catch (TelegramApiException e) {
                logger.error("Failed activating bot", e);
            }
            message = getBot().simpleEditMessage(question, getBot().getMassageFromUpdate(update), KeyboardFactory.createInlineKeyboardForHour(), "chooseHour");
        }else{
            if(type.equals("oneTime")){
                message = askForWhom(update);
            }else{
                if(type.equals("regularMonthly") || type.equals("regularYearly")){
                    message = askForWhom(update);
                }
            }
        }
        return message;
    }

    private Message processChooseHour(Update update, String[] commandValue) throws TelegramApiException {
        hour = Integer.parseInt(commandValue[1]);
        String question = "Zu welcher Minute?";
        try {
            getBot().sendAnswerCallbackQuery(hour + " gewählt. " + question, false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return getBot().simpleEditMessage(question, getBot().getMassageFromUpdate(update), KeyboardFactory.createInlineKeyboardForMinute(), "chooseMinute");
    }

    private Message processChooseMinute(Update update, String[] commandValue) throws TelegramApiException {
        minute = Integer.parseInt(commandValue[1]);
        return askForWhom(update);
    }

    private void processForMe(Update update, User user, Message message) throws TelegramApiException {
        task.getUserList().add(user);
        DBUtil.executeSQL(task.getInsertDBString());
        getSentMessages().add(message);
        ObjectHub.getInstance().getTasksRunnable().getTasksToDo().add(task);
        close();
        getBot().simpleEditMessage("Termin eingetragen :)", update, KeyboardFactory.KeyBoardType.NoButtons);
    }

    private void processForAll(Update update, Map<Integer, User> allowedUsersMap, Message message) throws TelegramApiException {
        allowedUsersMap.values().forEach(user1 -> task.getUserList().add(user1));
        DBUtil.executeSQL(task.getInsertDBString());
        getSentMessages().add(message);
        ObjectHub.getInstance().getTasksRunnable().getTasksToDo().add(task);
        close();
        getBot().simpleEditMessage("Termin eingetragen :)", update, KeyboardFactory.KeyBoardType.NoButtons);
    }

    private void processShowAppointments(Update update, User user){
        int currentUserId = user.getId();
        StringBuilder messageOfTasks = new StringBuilder();
        List<Task> taskList = DBUtil.getTasksFromDB(getBot());
        Collections.sort(taskList);
        Collections.reverse(taskList);
        for (Task task : taskList) {
            boolean taskForCurrentUser = task.getUserList().stream().anyMatch(user1 -> user1.getId() == currentUserId);
            if(!taskForCurrentUser){
                continue;
            }
            messageOfTasks.append("\n-----------------\n");
             if(task.getTaskStrategy() instanceof OneTimeTaskStrategy) {
                 LocalDateTime time = task.getTaskStrategy().getTime();
                 String min = time.getMinute() == 0 ? "" : "." + time.getMinute();
                 String date = time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, "));
                 int hour = time.getHour();
                 String germanDate = date + hour + min;
                 messageOfTasks.append("Am *").append(germanDate).append(" Uhr*:\n");
            }else{
                switch (task.getTaskStrategy().getType()){
                    case DAILY:
                        messageOfTasks.append("*Täglich*:\n");
                        break;
                    case MONTHLY:
                        messageOfTasks.append("*Monatlich, jeden ").append(((RegularMonthlyTaskStrategy) task.getTaskStrategy()).getDay()).append(".*:\n");
                        break;
                    case YEARLY:
                        messageOfTasks.append("*Jährlich, jeden ").append(TimeUtil.getMonthMapIntKeys().get(((RegularYearlyTaskStrategy) task.getTaskStrategy()).getMonth())).append(" am ").append(((RegularYearlyTaskStrategy) task.getTaskStrategy()).getDay()).append(".*:\n");
                        break;
                }
            }
            messageOfTasks.append(task.getName()).append("\n");
            StringBuilder userString = new StringBuilder();
            task.getUserList().forEach(user1 -> userString.append(", ").append(user1.getName()));
            messageOfTasks.append("_").append(userString.toString().replaceFirst(", ", "")).append("_");
        }
        String messageString = messageOfTasks.toString().replaceFirst("\n-----------------\n", "");
        getBot().sendMsg(messageString, update, KeyboardFactory.KeyBoardType.NoButtons, true, false, Bot.ParseMode.Markdown);
        close();
    }
    private Message processAddAppointment(Update update){
        return getBot().sendMsg("Art des Termins wählen:", update, KeyboardFactory.KeyBoardType.Calendar_Choose_Strategy, "chooseStrategy", true, true);
    }

    private Message processDeleteAppointment(Update update){
        List<String> taskNames = new ArrayList<>();
        DBUtil.getTasksFromDB(getBot()).forEach(task1 -> taskNames.add(task1.getName()));
        ReplyKeyboard listKeyboard = KeyboardFactory.getInlineKeyboardForList(taskNames, "deleteTask");
        return getBot().sendKeyboard("Welchen Termin willst du löschen?", update, listKeyboard, false);
    }

    private Message processDeleteTask(Update update, String[] commandValue) throws TelegramApiException {
        Message message = null;
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
                logger.error("Failed activating bot", e);
            }
            List<String> taskNames1 = new ArrayList<>();
            DBUtil.getTasksFromDB(getBot()).forEach(task1 -> taskNames1.add(task1.getName()));
            ReplyKeyboard listKeyboard1 = KeyboardFactory.getInlineKeyboardForList(taskNames1, "deleteTask");
            message = getBot().simpleEditMessage("Welchen Termin willst du löschen?", getBot().getMassageFromUpdate(update), listKeyboard1, "deleteTask");
        }
        return message;
    }

    private Message processOneTime(Update update){
        return getBot().sendMsg("Welches Jahr?", update, KeyboardFactory.KeyBoardType.Calendar_Year, "chooseYear", false, true);
    }

    private Message processRegularMonthly(Update update){
        return getBot().sendMsg("Welcher Tag?", update, new InlineKeyboardMarkup().setKeyboard(KeyboardFactory.createInlineKeyboardForYearMonth(LocalDate.now().getYear(), LocalDate.now().getMonth().getValue())), "day", false, true, Bot.ParseMode.None);
    }

    private Message processRegularYearly(Update update){
        return getBot().sendMsg("Welcher Monat?", update, KeyboardFactory.KeyBoardType.Calendar_Month, "chooseMonth", false, true);
    }

    private Message askForWhom(Update update) throws TelegramApiException {
            if(update.hasCallbackQuery()){
            getBot().sendAnswerCallbackQuery(day + " gewählt.", false, update.getCallbackQuery());
            }
        switch (type){
            case "oneTime":
            case "oneTimeWithTime":
                LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute);
                task.setTaskStrategy(new SimpleCalendarOneTimeStrategy(task, localDateTime));
                return getBot().simpleEditMessage("Für wen?", update, KeyboardFactory.KeyBoardType.User_Choose, "chooseUser");
            case "regularDaily":
                task.setTaskStrategy(new RegularDailyTaskStrategy(task));
                break;
            case "regularMonthly":
                task.setTaskStrategy(new RegularMonthlyTaskStrategy(task, day));
                break;
            case "regularYearly":
                task.setTaskStrategy(new RegularYearlyTaskStrategy(task, day, month));
                break;
        }
        return getBot().sendMsg("Für wen?", update, KeyboardFactory.KeyBoardType.User_Choose, "chooseUser", false, true);
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
                                if (inputString.startsWith("chooseDay")) {
                                    return "chooseDay";
                                } else {
                                if (inputString.startsWith("chooseHour")) {
                                    return "chooseHour";
                                } else {
                                if (inputString.startsWith("chooseMinute")) {
                                    return "chooseMinute";
                                } else {
                                    if (inputString.startsWith("forMe")) {
                                        return "forMe";
                                    } else {
                                        if (inputString.startsWith("forAll")) {

                                        } else {
                                            if (inputString.startsWith("daily")) {
                                                return "daily";
                                            } else {
                                                if (inputString.startsWith("monthly")) {
                                                    return "monthly";
                                                } else {
                                                    if (inputString.startsWith("yearly")) {
                                                        {
                                                            return "yearly";
                                                        }
                                                    }
                                                }
                                            }
                                                }
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
