package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.DBUtil;
import com.Utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
        Message message = null;
            switch (currentStep){
                case Start:
                    message = getBot().askMonth("Für welchem Monat...?", update, false);
                    currentStep = Steps.selectMonth;
                    break;
                case selectMonth:
                    if(TimeUtil.getMonthMap().keySet().contains(arg)) {
                        month = TimeUtil.getMonthMap().get(arg);
                        message = getBot().askYear("Für welches Jahr...?", update, false);
                        currentStep = Steps.selectYear;
                    }else{
                        message = getBot().askMonth("Für welchem Monat...?", update, false);
                    }
                    break;
                case selectYear:
                    if(TimeUtil.getYearsSet().contains(arg)){
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
                            try {
                                getBot().sendAnswerCallbackQuery("Fertig", false, update.getCallbackQuery());
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            getBot().sendMsg("Fertig: " + documentList.size() + " Bilder geholt.", update, null, false, false);
                        }
                    });
                    close();
                    getBot().setBusy(false);
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
        return "Get-Bons";
    }

    private enum Steps{
        selectMonth, selectYear, Start
    }
}
