package com.Misc.Processes;

import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
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

    public SumProcess(Bot bot){
        setBot(bot);
        currentStep = Steps.Start;
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
                String parsedDate = month + "/" + year;
                    DateFormat parser = new SimpleDateFormat("mm/yyyy");
                    DateFormat formatter = new SimpleDateFormat("yyyy-mm");
                Date convertedDate = null;
                try {
                    convertedDate = parser.parse(parsedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                parsedDate = formatter.format(convertedDate);
                float sumOfMonth = DBUtil.getSumMonth(parsedDate);
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Summe " + month + "/" + year + ":\n" + sumOfMonth, getBot());
                getBot().setBusy(false);
                getBot().process = null;
                break;
        }
    }

    private enum Steps{
        selectMonth, selectYear, Start
    }

}
