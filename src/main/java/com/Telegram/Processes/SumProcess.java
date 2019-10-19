package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import com.Utils.TimeUtil;
import com.google.inject.internal.cglib.proxy.$Callback;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
                message = BotUtil.askMonth("Für welchem Monat...?", update, getBot(), false);
                currentStep = Steps.selectMonth;
                break;
            case selectMonth:
                if(TimeUtil.getMonthMap().keySet().contains(update.getCallbackQuery().getData())) {
                    month = TimeUtil.getMonthMap().get(update.getCallbackQuery().getData());
                    message =  BotUtil.askYear("Für welches Jahr...?", update, getBot(), false);
                    currentStep = Steps.selectYear;
                }else{
                    message = BotUtil.askMonth("Für welchem Monat...?", update, getBot(), false);
                }
                break;
            case selectYear:
                year = update.getCallbackQuery().getData();
                if(TimeUtil.getYearsSet().contains(year = update.getCallbackQuery().getData())) {
                    getBot().setBusy(true);
                    String parsedDate = month + "." + year;
                    float sumOfMonth = DBUtil.getSumMonth(parsedDate);
                    try {
                        BotUtil.sendAnswerCallbackQuery("Summe " + month + "/" + year + ":\n" + sumOfMonth, getBot(), false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    BotUtil.sendMsg("Summe " + month + "/" + year + ":\n" + sumOfMonth, getBot(), update, null, false, false);
                    getBot().setBusy(false);
                    close();
                }else{
                    message = BotUtil.askYear("Für welches Jahr...?", update, getBot(), false);
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
