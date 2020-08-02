package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.backend.taskHandling.strategies.*;
import com.backend.taskHandling.Task;
import com.backend.ObjectHub;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;

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
import java.util.*;

public class CalenderProcess extends Process {

    private int year;

    private int month;

    private int day = 0;

    private int hour = 4;

    private int minute = 0;

    private Task task;

    private String type;

    private static Set<String> commands = Set.of(
            "Kalender",
            "Termin hinzufügen",
            "Termin löschen",
            "Termine anzeige"
    );

    public CalenderProcess(ProgressReporter reporter, BackendFacade facade) {
        super(reporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) throws TelegramApiException{
        User user = bot.getNonBotUserFromUpdate(update);
        String[] commandValue = deserializeInput(update, bot);
        Message message = null;
        try {
            switch (commandValue[0]) {
                case "done":
                    processDone(update, bot);
                    break;
                case "chooseStrategy":
                    message = processChooseStrategy(update, commandValue, bot);
                    break;
                case "daily":
                    message = processDayly(update, bot);
                    break;
                case "monthly":
                    message = processMonthly(update, bot);
                    break;
                case "yearly":
                    message = processYearly(update, bot);
                    break;
                case "chooseYear":
                    message = processChooseYear(update, commandValue, bot);
                    break;
                case "chooseMonth":
                    message = processChooseMonth(update, commandValue, bot);
                    break;
                case "chooseDay":
                    message = processChooseDay(update, commandValue, bot);
                    break;
                case "chooseHour":
                    message = processChooseHour(update, commandValue, bot);
                    break;
                 case "chooseMinute":
                     message = processChooseMinute(update, commandValue, bot);
                    break;
                case "forMe":
                    processForMe(update, user, message, bot);
                    break;
                case "forAll":
                    processForAll(update, bot, message, user);
                    break;
                case "Termine anzeige":
                    processShowAppointments(update, user, bot);
                    break;
                case "Termin hinzufügen":
                    message = processAddAppointment(update, bot);
                    break;
                case "Termin löschen":
                    message = processDeleteAppointment(update, bot);
                    break;
                case "deleteTask":
                   message = processDeleteTask(update, commandValue, bot);
                    break;
                case "-": //In case a faulty day was chosen by user
                    bot.sendAnswerCallbackQuery("Ungültiger Tag", false, update.getCallbackQuery());
                    break;
                case "Kalender":
                    bot.sendKeyboard("Was willst du tun?", update, KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Calendar, false, false, null, getFacade()), false);
                    reset(bot, user);
                    break;
                default:
                    task.setName(commandValue[0]);
                    switch (type){
                        case "oneTime":
                        case "oneTimeWithTime":
                            message = processOneTime(update, bot);
                            break;
                        case "regularDaily":
                            message = askForWhom(update, bot);
                            break;
                        case "regularMonthly":
                            message = processRegularMonthly(update, bot);
                            break;
                        case "regularYearly":
                            message = processRegularYearly(update, bot);
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
        bot.getNonBotUserFromUpdate(update).setBusy(false);
        if(message != null){
            getSentMessages().add(message);
        }
    }

    private void processDone(Update update, Bot bot) {
        bot.sendMsg("Ok :)", update, null, false, false);
        reset(bot, bot.getNonBotUserFromUpdate(update));
    }

    private Message processChooseStrategy(Update update, String[] commandValue, Bot bot) throws TelegramApiException {
        Message message = null;
        task = new Task(bot);
        switch (commandValue[1]) {
            case "oneTime":
                type = "oneTime";
                try {
                    bot.sendAnswerCallbackQuery("Bezeichnung wählen", false, update.getCallbackQuery());
                } catch (TelegramApiException e) {
                    logger.error("Failed activating bot", e);
                }
                message = bot.simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
                break;
            case "oneTimeWithTime":
                type = "oneTimeWithTime";
                try {
                    bot.sendAnswerCallbackQuery("Bezeichnung wählen", false, update.getCallbackQuery());
                } catch (TelegramApiException e) {
                    logger.error("Failed activating bot", e);
                }
                message = bot.simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
                break;
            case "regular":
                type = "regular";
                try {
                    bot.sendAnswerCallbackQuery("Wann?", false, update.getCallbackQuery());
                } catch (TelegramApiException e) {
                    logger.error("Failed activating bot", e);
                }
                message = bot.simpleEditMessage("Wann?", update, KeyboardFactory.KeyBoardType.Calendar_Regular_Choose_Unit);
                break;
        }
        return message;
    }

    private Message processDayly(Update update, Bot bot) throws TelegramApiException {
        type = "regularDaily";
        try {
            bot.sendAnswerCallbackQuery("Täglich gewählt.", false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return bot.simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
    }

    private Message processMonthly(Update update, Bot bot) throws TelegramApiException {
        type = "regularMonthly";
        try {
            bot.sendAnswerCallbackQuery("Monatlich gewählt.", false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return bot.simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
    }

    private Message processYearly(Update update, Bot bot) throws TelegramApiException {
        type = "regularYearly";
        try {
            bot.sendAnswerCallbackQuery("Jährlich gewählt.", false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return bot.simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
    }

    private Message processChooseYear(Update update, String[] commandValue, Bot bot) throws TelegramApiException {
        year = Integer.parseInt(commandValue[1]);
        String question = "Welcher Monat?";
        try {
            bot.sendAnswerCallbackQuery(year + " gewählt. " + question, false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return bot.simpleEditMessage(question, update, KeyboardFactory.KeyBoardType.Calendar_Month, "chooseMonth");
    }

    private Message processChooseMonth(Update update, String[] commandValue, Bot bot) throws TelegramApiException {
        month = Integer.parseInt(TimeUtil.getMonthMapStringKeys().get(commandValue[1]));
        String question = "Welcher Tag?";
        try {
            bot.sendAnswerCallbackQuery(month + " gewählt. " + question, false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return bot.simpleEditMessage(question, bot.getMassageFromUpdate(update), KeyboardFactory.createInlineKeyboardForYearMonth(year, month), "chooseDay");
    }

    private Message processChooseDay(Update update, String[] commandValue, Bot bot) throws TelegramApiException {
        Message message = null;
        String question = "Zu welcher Stunde?";
        if(commandValue[1].equals("-")) {
            bot.sendAnswerCallbackQuery("Ungültiger Tag", false, update.getCallbackQuery());
            return null;
        }
        day = Integer.parseInt(commandValue[1]);
        if(type.equals("oneTimeWithTime")){
            try {
                bot.sendAnswerCallbackQuery(day + " gewählt. " + question, false, update.getCallbackQuery());
            } catch (TelegramApiException e) {
                logger.error("Failed activating bot", e);
            }
            message = bot.simpleEditMessage(question, bot.getMassageFromUpdate(update), KeyboardFactory.createInlineKeyboardForHour(), "chooseHour");
        }else{
            if(type.equals("oneTime")){
                message = askForWhom(update, bot);
            }else{
                if(type.equals("regularMonthly") || type.equals("regularYearly")){
                    message = askForWhom(update, bot);
                }
            }
        }
        return message;
    }

    private Message processChooseHour(Update update, String[] commandValue, Bot bot) throws TelegramApiException {
        hour = Integer.parseInt(commandValue[1]);
        String question = "Zu welcher Minute?";
        try {
            bot.sendAnswerCallbackQuery(hour + " gewählt. " + question, false, update.getCallbackQuery());
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
        return bot.simpleEditMessage(question, bot.getMassageFromUpdate(update), KeyboardFactory.createInlineKeyboardForMinute(), "chooseMinute");
    }

    private Message processChooseMinute(Update update, String[] commandValue, Bot bot) throws TelegramApiException {
        minute = Integer.parseInt(commandValue[1]);
        return askForWhom(update, bot);
    }

    private void processForMe(Update update, User user, Message message, Bot bot) throws TelegramApiException {
        task.getUserList().add(user);
        getFacade().insertTask(task);
        getSentMessages().add(message);
        ObjectHub.getInstance().getTasksRunnable().getTasksToDo().add(task);
        reset(bot, user);
        bot.simpleEditMessage("Termin eingetragen :)", update, KeyboardFactory.KeyBoardType.NoButtons);
    }

    private void processForAll(Update update, Bot bot, Message message, User user) throws TelegramApiException {
        bot.getAllowedUsersMap().values().forEach(user1 -> task.getUserList().add(user1));
        getFacade().insertTask(task);
        getSentMessages().add(message);
        ObjectHub.getInstance().getTasksRunnable().getTasksToDo().add(task);
        reset(bot, user);
        bot.simpleEditMessage("Termin eingetragen :)", update, KeyboardFactory.KeyBoardType.NoButtons);
    }

    private void processShowAppointments(Update update, User user, Bot bot){
        int currentUserId = user.getId();
        StringBuilder messageOfTasks = new StringBuilder();
        List<Task> taskList = getFacade().getTasks();
        Collections.sort(taskList);
        Collections.reverse(taskList);
        for (Task task : taskList) {
            boolean taskForCurrentUser = task.getUserList().stream().anyMatch(user1 -> user1.getId() == currentUserId);
            if(!taskForCurrentUser){
                continue;
            }
            messageOfTasks.append("\n-----------------\n");
             if(task.getExecutionStrategy() instanceof OneTimeExecutionStrategy) {
                 LocalDateTime time = task.getExecutionStrategy().getTime();
                 String min = time.getMinute() == 0 ? "" : "." + time.getMinute();
                 String date = time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, "));
                 int hour = time.getHour();
                 String germanDate = date + hour + min;
                 messageOfTasks.append("Am *").append(germanDate).append(" Uhr*:\n");
            }else{
                switch (task.getExecutionStrategy().getType()){
                    case DAILY:
                        messageOfTasks.append("*Täglich*:\n");
                        break;
                    case MONTHLY:
                        messageOfTasks.append("*Monatlich, jeden ").append(((RegularMonthlyExecutionStrategy) task.getExecutionStrategy()).getDay()).append(".*:\n");
                        break;
                    case YEARLY:
                        messageOfTasks.append("*Jährlich, jeden ").append(TimeUtil.getMonthMapIntKeys().get(((RegularYearlyExecutionStrategy) task.getExecutionStrategy()).getMonth())).append(" am ").append(((RegularYearlyExecutionStrategy) task.getExecutionStrategy()).getDay()).append(".*:\n");
                        break;
                }
            }
            messageOfTasks.append(task.getName()).append("\n");
            StringBuilder userString = new StringBuilder();
            task.getUserList().forEach(user1 -> userString.append(", ").append(user1.getName()));
            messageOfTasks.append("_").append(userString.toString().replaceFirst(", ", "")).append("_");
        }
        String messageString = messageOfTasks.toString().replaceFirst("\n-----------------\n", "");
        bot.sendMsg(messageString, update, KeyboardFactory.KeyBoardType.NoButtons, true, false, Bot.ParseMode.Markdown);
        reset(bot, user);
    }
    private Message processAddAppointment(Update update, Bot bot){
        return bot.sendMsg("Art des Termins wählen:", update, KeyboardFactory.KeyBoardType.Calendar_Choose_Strategy, "chooseStrategy", true, true);
    }

    private Message processDeleteAppointment(Update update, Bot bot){
        List<String> taskNames = new ArrayList<>();
        getFacade().getTasks().forEach(task1 -> taskNames.add(task1.getName()));
        ReplyKeyboard listKeyboard = KeyboardFactory.getInlineKeyboardForList(taskNames, "deleteTask");
        return bot.sendKeyboard("Welchen Termin willst du löschen?", update, listKeyboard, false);
    }

    private Message processDeleteTask(Update update, String[] commandValue, Bot bot) throws TelegramApiException {
        Message message = null;
        Task taskToRemove = null;
        for (Task task : getFacade().getTasks()) {
            if (task.getName().equals(commandValue[1])) {
                taskToRemove = task;
                break;
            }
        }
        if (taskToRemove != null) {
            getFacade().deleteTask(taskToRemove);
            try {
                bot.sendAnswerCallbackQuery(taskToRemove.getName() + " gelöscht :)", false, update.getCallbackQuery());
            } catch (TelegramApiException e) {
                logger.error("Failed activating bot", e);
            }
            List<String> taskNames1 = new ArrayList<>();
            getFacade().getTasks().forEach(task1 -> taskNames1.add(task1.getName()));
            ReplyKeyboard listKeyboard1 = KeyboardFactory.getInlineKeyboardForList(taskNames1, "deleteTask");
            message = bot.simpleEditMessage("Welchen Termin willst du löschen?", bot.getMassageFromUpdate(update), listKeyboard1, "deleteTask");
        }
        return message;
    }

    private Message processOneTime(Update update, Bot bot){
        return bot.sendMsg("Welches Jahr?", update, KeyboardFactory.KeyBoardType.Calendar_Year, "chooseYear", false, true);
    }

    private Message processRegularMonthly(Update update, Bot bot){
        return bot.sendMsg("Welcher Tag?", update, new InlineKeyboardMarkup().setKeyboard(KeyboardFactory.createInlineKeyboardForYearMonth(LocalDate.now().getYear(), LocalDate.now().getMonth().getValue())), "day", false, true, Bot.ParseMode.None);
    }

    private Message processRegularYearly(Update update, Bot bot){
        return bot.sendMsg("Welcher Monat?", update, KeyboardFactory.KeyBoardType.Calendar_Month, "chooseMonth", false, true);
    }

    private Message askForWhom(Update update, Bot bot) throws TelegramApiException {
            if(update.hasCallbackQuery()){
            bot.sendAnswerCallbackQuery(day + " gewählt.", false, update.getCallbackQuery());
            }
        switch (type){
            case "oneTime":
            case "oneTimeWithTime":
                LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute);
                task.setExecutionStrategy(new SimpleCalendarOneTimeStrategy(task, localDateTime, getFacade()));
                return bot.simpleEditMessage("Für wen?", update, KeyboardFactory.KeyBoardType.User_Choose, "chooseUser");
            case "regularDaily":
                task.setExecutionStrategy(new RegularDailyExecutionStrategy(task));
                break;
            case "regularMonthly":
                task.setExecutionStrategy(new RegularMonthlyExecutionStrategy(task, day));
                break;
            case "regularYearly":
                task.setExecutionStrategy(new RegularYearlyExecutionStrategy(task, day, month));
                break;
        }
        return bot.sendMsg("Für wen?", update, KeyboardFactory.KeyBoardType.User_Choose, "chooseUser", false, true);
    }

    @Override
    public String getProcessName() {
        return "Calender Process";
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        String inputString = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : update.getMessage().getText();
        if (inputString.startsWith("Termin")) {
            return inputString;
        } else {
            if (inputString.startsWith("deleteTask")) {
                return "deleteTask";
            } else {
                if (inputString.startsWith("chooseName;calendar")) {
                    return "chooseName;calendar";
                } else {
                    if (inputString.startsWith("chooseStrategy;calendar")) {
                        return "chooseStrategy;calendar";
                    } else {
                        if (inputString.startsWith("chooseYear;calendar")) {
                            return "chooseYear;calendar";
                        } else {
                            if (inputString.startsWith("chooseMonth;calendar")) {
                                return "chooseMonth;calendar";
                            } else {
                                if (inputString.startsWith("chooseDay;calendar")) {
                                    return "chooseDay;calendar";
                                } else {
                                if (inputString.startsWith("chooseHour;calendar")) {
                                    return "chooseHour;calendar";
                                } else {
                                if (inputString.startsWith("chooseMinute;calendar")) {
                                    return "chooseMinute;calendar";
                                } else {
                                    if (inputString.startsWith("forMe;calendar")) {
                                        return "forMe;calendar";
                                    } else {
                                        if (inputString.startsWith("forAll;calendar")) {
                                            return "forAll;calendar";
                                        } else {
                                            if (inputString.startsWith("daily;calendar")) {
                                                return "daily;calendar";
                                            } else {
                                                if (inputString.startsWith("monthly;calendar")) {
                                                    return "monthly;calendar";
                                                } else {
                                                    if (inputString.startsWith("yearly;calendar")) {
                                                        {
                                                            return "yearly;calendar";
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

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }
}
