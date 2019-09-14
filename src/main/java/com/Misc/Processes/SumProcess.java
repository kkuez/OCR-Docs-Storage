package com.Misc.Processes;

import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SumProcess extends Process{

    private String month;

    private String year;

    private Steps currentStep;

    public SumProcess(){
        currentStep = Steps.selectMonth;
    }
    @Override
    public void performNextStep(String arg, Update update) {
        switch (currentStep){
            case selectMonth:
                month = getNumberForMonth(arg);
                BotUtil.askYear("Für welches Jahr...?", update, Bot.bot);
                currentStep = Steps.selectYear;
                break;

            case selectYear:
                year = arg;

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
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Summe " + month + "/" + year + ":\n" + sumOfMonth, Bot.bot);

                Bot.process = null;
                break;
        }
    }

    private enum Steps{
        selectMonth, selectYear
    }
    private String getNumberForMonth(String month){
        switch (month){
            case "JAN":
                return "01";
            case "FEB":
                return "02";
            case "MÄR":
                return "03";
            case "APR":
                return "04";
            case "MAI":
                return "05";
            case "JUN":
                return "06";
            case "JUL":
                return "07";
            case "AUG":
                return "08";
            case "SEP":
                return "09";
            case "OKT":
                return "10";
            case "NOV":
                return "11";
            case "DEZ":
                return "12";
        }
        return null;
    }
}
