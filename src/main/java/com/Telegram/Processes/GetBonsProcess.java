package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Document;
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
import java.util.List;

public class GetBonsProcess extends Process{
    private Steps currentStep;

    private String month;

    private String year;

    public GetBonsProcess(Bot bot, ProgressReporter progressReporter){
        super(progressReporter);
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
                    getBot().setBusy(true);
                    year = arg;
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", "Hole Bons...", getBot());
                    String parsedDate = month + "/" + year;
                    DateFormat parser = new SimpleDateFormat("mm/yyyy");
                    DateFormat formatter = new SimpleDateFormat("yyyy-mm");
                    Date convertedDate = null;
                    try {
                        convertedDate = parser.parse(parsedDate);
                    } catch (ParseException e) {
                        LogUtil.logError(parsedDate, e);
                    }
                    parsedDate = formatter.format(convertedDate);
                    List<Document> documentList = DBUtil.getDocumentsForMonthAndYear(parsedDate);

                    documentList.forEach(document1 -> {
                        String possibleCaption = " ";
                        if(ObjectHub.getInstance().getAllowedUsersMap().keySet().contains(document1.getUser())){
                            possibleCaption = "Von " + ObjectHub.getInstance().getAllowedUsersMap().get(document1.getUser()).getName();
                        }
                        getBot().sendPhotoFromURL(update, document1.getOriginFile().getAbsolutePath(), possibleCaption, null);
                    });
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", "Fertig: " + documentList.size() + " Bilder geholt.", getBot());
                    getBot().process = null;
                    getBot().setBusy(false);
                    break;
        }

    }

    @Override
    public String getProcessName() {
        return "Get-Bons";
    }

    private enum Steps{
        selectMonth, selectYear, Start
    }
}
