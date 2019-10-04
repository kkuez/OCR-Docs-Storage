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
import org.telegram.telegrambots.meta.api.objects.Update;

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
        switch (currentStep){
            case Start:
                BotUtil.askMonth("Für welchem Monat...?", update, getBot(), false);
                currentStep = Steps.selectMonth;
                break;
            case selectMonth:
                month = TimeUtil.getMonthMap().get(update.getCallbackQuery().getData());
                BotUtil.askYear("Für welches Jahr...?", update, getBot(), false);
                currentStep = Steps.selectYear;
                break;
            case selectYear:
                year = update.getCallbackQuery().getData();
                getBot().setBusy(true);
                String parsedDate = month + "." + year;
                float sumOfMonth = DBUtil.getSumMonth(parsedDate);
                BotUtil.sendMsg("Summe " + month + "/" + year + ":\n" + sumOfMonth, getBot(), update, null, false, false);
                getBot().setBusy(false);
                setDeleteLater(true);
                break;
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
