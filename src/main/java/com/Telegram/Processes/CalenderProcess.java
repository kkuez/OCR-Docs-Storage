package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.Misc.TaskHandling.Strategies.OneTimeTaskStrategy;
import com.Misc.TaskHandling.Strategies.RegularTaskStrategy;
import com.Misc.TaskHandling.Strategies.SimpleCalendarOneTimeStrategy;
import com.Misc.TaskHandling.Strategies.TaskStrategy;
import com.Misc.TaskHandling.Task;
import com.ObjectHub;
import com.ObjectTemplates.User;
import com.Taskshub;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.DBUtil;
import com.Utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CalenderProcess extends Process {

    private int year;

    private int month;

    private int day;

    private int hour;

    private int minute;

    private Task task;

    public CalenderProcess(ProgressReporter reporter, Bot bot, Update update, Map<Integer, User> allowedUsersMap) {
        super(reporter);
        setBot(bot);
        performNextStep("Termin hinzufügen", update, allowedUsersMap);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        User user = getBot().getNonBotUserFromUpdate(update);
        String[] commandValue = deserializeInput(update);
        Message message = null;
        switch (commandValue[0]){
            case "chooseStrategy":
                task = new Task(user, getBot());
                switch (commandValue[1]){
                    case "oneTime":
                        task.setTaskStrategy(new SimpleCalendarOneTimeStrategy(task));
                        break;
                    case "regular":
                        //task.setTaskStrategy(new RegularTaskStrategy(task));
                        break;
                }
                try {
                    getBot().sendAnswerCallbackQuery("Bezeichnung wählen", false, update.getCallbackQuery());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                message = getBot().simpleEditMessage("Bezeichnung wählen:", update, KeyboardFactory.KeyBoardType.Abort);
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
                LocalDateTime localDateTime = LocalDateTime.of(year, month, day, 4, 0);
                task.setTime(localDateTime);
                message = getBot().simpleEditMessage("Für wen?", update, KeyboardFactory.KeyBoardType.User_Choose, "chooseUser");
                break;
            case "forMe":
                task.getUserList().add(user);
                DBUtil.executeSQL(task.getInsertDBString());
                getSentMessages().add(message);
                ObjectHub.getInstance().getTaskshub().getTasksToDo().add(task);
                close();
                getBot().simpleEditMessage("Termin eingetragen :)", update, KeyboardFactory.KeyBoardType.NoButtons);
                break;
            case "forAll":
                allowedUsersMap.values().forEach(user1 -> task.getUserList().add(user1));
                DBUtil.executeSQL(task.getInsertDBString());
                getSentMessages().add(message);
                ObjectHub.getInstance().getTaskshub().getTasksToDo().add(task);
                close();
                getBot().simpleEditMessage("Termin eingetragen :)", update, KeyboardFactory.KeyBoardType.NoButtons);
                break;
            case "Termine anzeige":
                break;
            case "Termin hinzufügen":
                getBot().sendMsg("Art des Termins wählen:", update, KeyboardFactory.KeyBoardType.Calendar_Choose_Strategy,"chooseStrategy", true, true);
                break;
            case "Termin löschen":
                break;
                default:
                task.setName(commandValue[0]);
                    message = getBot().sendMsg("Welches Jahr?", update, KeyboardFactory.KeyBoardType.Calendar_Year, "chooseYear",false, true );
                    break;
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
            if(inputString.startsWith("Termin")){
                return inputString;
            }else{
                if(inputString.startsWith("chooseName")){
                    return "chooseName";
                }else {
                    if (inputString.startsWith("chooseStrategy")){
                    return "chooseStrategy";
                }else {
                    if (inputString.startsWith("chooseYear")){
                    return "chooseYear";
                }else {
                    if (inputString.startsWith("chooseMonth")){
                    return "chooseMonth";
                }else {
                    if (inputString.startsWith("day")){
                    return "day";
                }else {
                    if (inputString.startsWith("forMe")){
                    return "forMe";
                }else {
                    if (inputString.startsWith("forAll")){
                        {
                }

            }}}}}}}}
        return inputString;
    }
}
