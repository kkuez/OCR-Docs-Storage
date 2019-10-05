package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import com.Utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.print.Doc;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GetBonsProcess extends Process{
    private Steps currentStep;

    private String month;

    private String year;

    public GetBonsProcess(Bot bot, ProgressReporter progressReporter, Update update, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        currentStep = Steps.Start;
        performNextStep("" , update, allowedUsersMap);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
            switch (currentStep){
                case Start:
                    BotUtil.askMonth("Für welchem Monat...?", update, getBot(), false);
                    currentStep = Steps.selectMonth;
                    break;
                case selectMonth:
                    month = TimeUtil.getMonthMap().get(arg);
                    BotUtil.askYear("Für welches Jahr...?", update, getBot(), false);
                    currentStep = Steps.selectYear;
                    break;
                case selectYear:
                    year = arg;
                    getBot().setBusy(true);
                    String parsedDate = month + "." + year;
                    ObjectHub.getInstance().getExecutorService().submit(new Runnable() {
                        @Override
                        public void run() {
                            List<Document> documentList = DBUtil.getDocumentsForMonthAndYear(parsedDate);
                            for(Document document : documentList){
                                if(DBUtil.countDocuments("Bons", "where belongsToDocument =" + document.getId()) == 0){
                                    documentList.remove(document);
                                }
                            }
                            documentList.forEach(document1 -> {
                                String possibleCaption = " ";
                                if(ObjectHub.getInstance().getAllowedUsersMap().keySet().contains(document1.getUser())){
                                    possibleCaption = "Von " + ObjectHub.getInstance().getAllowedUsersMap().get(document1.getUser()).getName();
                                }
                                getBot().sendPhotoFromURL(update, document1.getOriginFile().getAbsolutePath(), possibleCaption, null);
                            });
                            BotUtil.sendMsg("Fertig: " + documentList.size() + " Bilder geholt.",getBot() , update, null, true, false);
                        }
                    });
                    setDeleteLater(true);
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
