package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.User;
import com.bot.telegram.Bot;

import com.utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetBonsProcess extends Process{
    private Steps currentStep;

    private String month;

    private String year;

    Map<Integer, User> allowedUsersMap;

    public GetBonsProcess(Bot bot, ProgressReporter progressReporter, Update update, Map<Integer, User> allowedUsersMap, BackendFacade facade){
        super(progressReporter, facade);
        this.allowedUsersMap = allowedUsersMap;
        setBot(bot);
        currentStep = Steps.Start;
        try {
            performNextStep("" , update, allowedUsersMap);
        } catch (TelegramApiException e) {
            if(((TelegramApiException) e).getCause().getLocalizedMessage().contains("message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message")){
                logger.info("Message not edited, no need.");
            }else{
                logger.error(((TelegramApiException) e).getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) throws TelegramApiException{
        String[] commandValue = deserializeInput(update);
        Message message = null;
        User user = getBot().getNonBotUserFromUpdate(update);
        switch (commandValue[0]){
            case "selectMonth":
                if(TimeUtil.getMonthMapStringKeys().keySet().contains(commandValue[1])) {
                    month = TimeUtil.getMonthMapStringKeys().get(commandValue[1]);
                message = getBot().askYear("Für welches Jahr...?", update, false, "selectYear");
                currentStep = Steps.selectYear;
            }else{
                message = getBot().askMonth("Für welchem Monat...?", update, false, "selectMonth");
            }
            break;
            case "selectYear":
                if(TimeUtil.getYearsSet().contains(commandValue[1])){
                    year = commandValue[1];
                    user.setBusy(true);
                    List<Bon> bonsForMonth = getFacade().getBonsForMonth(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1));
                    bonsForMonth.forEach(bon -> {
                        String possibleCaption = "Von " + allowedUsersMap.get(bon.getUser()).getName() + ": " + bon.getSum() + "€";
                                getBot().sendPhotoFromURL(update, bon.getOriginFile().getAbsolutePath(), possibleCaption, null);
                            });
                            try {
                                getBot().sendAnswerCallbackQuery("Fertig", false, update.getCallbackQuery());
                            } catch (TelegramApiException e) {
                                logger.error("Failed activating bot", e);
                            }
                            getBot().sendMsg("Fertig: " + bonsForMonth.size() + " Bilder geholt.", update, null, false, false);
                    close();
                    user.setBusy(false);
                }else{
                    message = getBot().askYear("Für welches Jahr...?", update, false, "selectYear");
                }
            break;
            default:
                message = getBot().askMonth("Für welchem Monat...?", update, false, "selectMonth");
                currentStep = Steps.selectMonth;
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

    @Override
    public String getCommandIfPossible(Update update) {
        String updateText = update.hasCallbackQuery() ? update.getCallbackQuery().getData() :  getBot().getMassageFromUpdate(update).getText();
        if(update.hasCallbackQuery()){
            if (updateText.startsWith("selectMonth")){
                return "selectMonth";
            }else{
                if (updateText.startsWith("selectYear")){
                    return "selectYear";
                }
            }
        }
        return "";
    }

    private enum Steps{
        selectMonth, selectYear, Start
    }
}
