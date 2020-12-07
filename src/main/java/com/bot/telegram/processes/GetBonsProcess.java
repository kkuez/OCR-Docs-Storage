package com.bot.telegram.processes;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.reporter.ProgressReporter;
import com.objectTemplates.Bon;
import com.objectTemplates.User;
import com.utils.TimeUtil;

public class GetBonsProcess extends Process {

    private String month;

    private String year;

    private static Set<String> commands = Set.of("selectMonth", "selectYear", "Hole Bons");

    public GetBonsProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) throws TelegramApiException {
        User user = bot.getNonBotUserFromUpdate(update);
        String[] commandValue = deserializeInput(update, bot);
        Message message = null;
        switch (commandValue[0]) {
            case "selectMonth":
                if (TimeUtil.getMonthMapStringKeys().keySet().contains(commandValue[1])) {
                    month = TimeUtil.getMonthMapStringKeys().get(commandValue[1]);
                    message = bot.askYear("Für welches Jahr...?", update, false, "selectYear");
                } else {
                    message = bot.askMonth("Für welchem Monat...?", update, false, "selectMonth");
                }
                break;
            case "selectYear":
                if (TimeUtil.getYearsSet().contains(commandValue[1])) {
                    year = commandValue[1];
                    user.setBusy(true);
                    List<Bon> bonsForMonth = getFacade()
                            .getSum(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1));
                    bonsForMonth.forEach(bon -> {
                        String possibleCaption = "Von " + bot.getAllowedUsersMap().get(bon.getUser()).getName() + ": "
                                + bon.getSum() + "€";
                        bot.sendPhotoFromURL(update, bon.getOriginFile().getAbsolutePath(), possibleCaption, null);
                    });
                    try {
                        bot.sendAnswerCallbackQuery("Fertig", false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        logger.error("Failed activating bot", e);
                    }
                    bot.sendMsg("Fertig: " + bonsForMonth.size() + " Bilder geholt.", update, null, false, false);
                    reset(bot, user);
                    user.setBusy(false);
                } else {
                    message = bot.askYear("Für welches Jahr...?", update, false, "selectYear");
                }
                break;
            case "Hole Bons":
                message = bot.askMonth("Für welchem Monat...?", update, false, "selectMonth");
                break;
        }
        if (message != null) {
            getSentMessages().add(message);
        }
    }

    @Override
    public String getProcessName() {
        return "Get-Bons";
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        String updateText = update.hasCallbackQuery() ? update.getCallbackQuery().getData()
                : bot.getMassageFromUpdate(update).getText();
        if (update.hasCallbackQuery()) {
            if (updateText.startsWith("selectMonth")) {
                return "selectMonth";
            } else {
                if (updateText.startsWith("selectYear")) {
                    return "selectYear";
                }
            }
        }
        return "";
    }

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }
}
