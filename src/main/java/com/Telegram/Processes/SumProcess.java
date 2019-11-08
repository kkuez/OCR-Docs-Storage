package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import com.Utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

public class SumProcess extends Process{

    private String month;

    private String year;


    public SumProcess(Bot bot, ProgressReporter progressReporter, Update update, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        Message message = null;
        try {
            message = getBot().askMonth("Für welchem Monat...?", update, false, "selectMonth");
            getSentMessages().add(message);
            performNextStep("", update, allowedUsersMap);
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
        Message message = null;
        String[] commandValue = deserializeInput(update);
        switch (commandValue[0]){
            case "selectMonth":
                if(TimeUtil.getMonthMapStringKeys().containsKey(commandValue[1])) {
                    month = TimeUtil.getMonthMapStringKeys().get(commandValue[1]);
                    message =  getBot().askYear("Für welches Jahr...?", update, false, "selectYear");
                }else{
                    message = getBot().askMonth("Für welchem Monat...?", update, false, "selectMonth");
                }
                break;
            case "selectYear":
                year = commandValue[1];
                if(TimeUtil.getYearsSet().contains(year)) {
                    getBot().getNonBotUserFromUpdate(update).setBusy(true);
                    String parsedDate = month + "." + year;
                    float sumOfMonthAll = DBUtil.getSumMonth(parsedDate, null);
                    User user = allowedUsersMap.get(update.getCallbackQuery().getFrom().getId());
                    float sumOfMonthForCurrentUser = DBUtil.getSumMonth(parsedDate, user);
                    String messageToSend = "Summe alle: " + month + "/" + year + ":\n" + sumOfMonthAll + "\nSumme " + user.getName() + " "+ month + "/" + year + ": " + sumOfMonthForCurrentUser;
                    try {
                        getBot().sendAnswerCallbackQuery(messageToSend, false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    getBot().sendMsg(messageToSend, update, null, false, false);
                    getBot().getNonBotUserFromUpdate(update).setBusy(false);
                    close();
                }else{
                    message = getBot().askYear("Für welches Jahr...?", update, false, "selectYear");
                }
                break;
        }
        if(message != null){
            getSentMessages().add(message);
        }
    }

    @Override
    public String getProcessName() {
        return "Get sum";
    }

    @Override
    public String getCommandIfPossible(Update update) {
        String updateText = update.hasCallbackQuery() ? update.getCallbackQuery().getData() :  getBot().getMassageFromUpdate(update).getText();
        if(update.hasCallbackQuery()){
            if(updateText.startsWith("selectMonth")){
                return "selectMonth";
            }else{
                if(updateText.startsWith("selectYear")){
                    return "selectYear";
                }
            }
        }
        return "";
    }
}
