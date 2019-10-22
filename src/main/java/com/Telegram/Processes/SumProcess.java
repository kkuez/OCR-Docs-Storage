package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.DBUtil;
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
        Message message = getBot().askMonth("Für welchem Monat...?", update, false, "selectMonth");
        getSentMessages().add(message);
        performNextStep("", update, allowedUsersMap);
    }
    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        Message message = null;
        String[] commandValue = deserializeInput(update);
        switch (commandValue[0]){
            case "selectMonth":
                if(TimeUtil.getMonthMap().keySet().contains(commandValue[1])) {
                    month = TimeUtil.getMonthMap().get(commandValue[1]);
                    message =  getBot().askYear("Für welches Jahr...?", update, false, "selectYear");
                }else{
                    message = getBot().askMonth("Für welchem Monat...?", update, false, "selectMonth");
                }
                break;
            case "selectYear":
                year = commandValue[1];
                if(TimeUtil.getYearsSet().contains(year)) {
                    getBot().setBusy(true);
                    String parsedDate = month + "." + year;
                    float sumOfMonth = DBUtil.getSumMonth(parsedDate);
                    try {
                        getBot().sendAnswerCallbackQuery("Summe " + month + "/" + year + ":\n" + sumOfMonth, false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    getBot().sendMsg("Summe " + month + "/" + year + ":\n" + sumOfMonth, update, null, false, false);
                    getBot().setBusy(false);
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
