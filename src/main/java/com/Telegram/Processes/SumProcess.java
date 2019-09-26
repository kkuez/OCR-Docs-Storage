package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import com.Utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SumProcess extends Process{

    private String month;

    private String year;

    private Steps currentStep;

    public SumProcess(Bot bot, ProgressReporter progressReporter, Update update){
        super(progressReporter);
        setBot(bot);
        currentStep = Steps.Start;
        performNextStep("", update);
    }
    @Override
    public void performNextStep(String arg, Update update) {
        switch (currentStep){
            case Start:
                BotUtil.askMonth("Für welchem Monat...?", update, getBot());
                currentStep = Steps.selectMonth;
                break;
            case selectMonth:
                month = TimeUtil.getMonthMap().get(arg);
                BotUtil.askYear("Für welches Jahr...?", update, getBot());
                currentStep = Steps.selectYear;
                break;

            case selectYear:
                year = arg;
                getBot().setBusy(true);
                String parsedDate = month + "." + year;
                float sumOfMonth = DBUtil.getSumMonth(parsedDate);
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Summe " + month + "/" + year + ":\n" + sumOfMonth, getBot());
                getBot().setBusy(false);
                getBot().process = null;
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
