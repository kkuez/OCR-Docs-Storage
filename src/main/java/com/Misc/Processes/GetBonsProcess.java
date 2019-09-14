package com.Misc.Processes;

import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GetBonsProcess extends Process{
    private Steps currentStep;

    private String month;

    private String year;

    public GetBonsProcess(){
        currentStep = Steps.selectMonth;
    }

    @Override
    public void performNextStep(String arg, Update update) {
            switch (currentStep){
                case selectMonth:
                    month = TimeUtil.getMonthMap().get(arg);
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
                    List<Document> documentList = DBUtil.getDocumentsForMonthAndYear(parsedDate);

                    documentList.forEach(document1 -> {
                        Bot.bot.sendPhotoFromURL(update, document1.getOriginFile().getAbsolutePath());
                        if(ObjectHub.getInstance().getAllowedUsersMap().keySet().contains(document1.getUser())){
                            BotUtil.sendMsg(update.getMessage().getChatId() + "","Von " + ObjectHub.getInstance().getAllowedUsersMap().get(document1.getUser()).getName(), Bot.bot);
                        }

                    });
                    Bot.process = null;
                    break;
        }

    }
    private enum Steps{
        selectMonth, selectYear
    }
}
