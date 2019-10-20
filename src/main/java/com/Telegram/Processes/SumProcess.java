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

    private Steps currentStep;

    public SumProcess(Bot bot, ProgressReporter progressReporter, Update update, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        currentStep = Steps.Start;
        performNextStep("", update, allowedUsersMap);
    }
    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        Message message = null;
        switch (currentStep){
            case Start:
                message = getBot().askMonth("Für welchem Monat...?", update, false);
                currentStep = Steps.selectMonth;
                break;
            case selectMonth:
                if(TimeUtil.getMonthMap().keySet().contains(update.getCallbackQuery().getData())) {
                    month = TimeUtil.getMonthMap().get(update.getCallbackQuery().getData());
                    message =  getBot().askYear("Für welches Jahr...?", update, false);
                    currentStep = Steps.selectYear;
                }else{
                    message = getBot().askMonth("Für welchem Monat...?", update, false);
                }
                break;
            case selectYear:
                year = update.getCallbackQuery().getData();
                if(TimeUtil.getYearsSet().contains(year = update.getCallbackQuery().getData())) {
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
                    message = getBot().askYear("Für welches Jahr...?", update, false);
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

    private enum Steps{
        selectMonth, selectYear, Start
    }

}
