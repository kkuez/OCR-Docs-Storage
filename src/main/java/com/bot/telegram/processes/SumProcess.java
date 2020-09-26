package com.bot.telegram.processes;

import java.time.LocalDate;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.utils.TimeUtil;

public class SumProcess extends Process {

    private String month;

    private String year;

    private static Set<String> commands = Set.of("selectMonth", "Summe von Bons", "selectYear");

    public SumProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        Message message = null;
        String[] commandValue = deserializeInput(update, bot);
        try {
            switch (commandValue[0]) {
                case "selectMonth":
                    if (TimeUtil.getMonthMapStringKeys().containsKey(commandValue[1])) {
                        month = TimeUtil.getMonthMapStringKeys().get(commandValue[1]);
                        message = bot.askYear("Für welches Jahr...?", update, false, "selectYear");
                    } else {
                        message = bot.askMonth("Für welchem Monat...?", update, false, "selectMonth");
                    }
                    break;
                case "selectYear":
                    year = commandValue[1];
                    if (TimeUtil.getYearsSet().contains(year)) {
                        User user = bot.getAllowedUsersMap().get(update.getCallbackQuery().getFrom().getId());
                        bot.getNonBotUserFromUpdate(update).setBusy(true);
                        float sumOfMonthAll = getFacade()
                                .getSumMonth(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1), null);
                        float sumOfMonthForCurrentUser = getFacade()
                                .getSumMonth(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1), user);
                        String messageToSend = month + "/" + year + "\nSumme alle: " + sumOfMonthAll + "\nSumme "
                                + user.getName() + ": " + sumOfMonthForCurrentUser;
                        try {
                            bot.sendAnswerCallbackQuery(messageToSend, false, update.getCallbackQuery());
                        } catch (TelegramApiException e) {
                            logger.error("Failed activating bot", e);
                        }
                        bot.sendMsg(messageToSend, update, null, false, false);
                        bot.getNonBotUserFromUpdate(update).setBusy(false);
                        reset(bot, user);
                    } else {
                        message = bot.askYear("Für welches Jahr...?", update, false, "selectYear");
                    }
                    break;
                default:
                    try {
                        message = bot.askMonth("Für welchem Monat...?", update, false, "selectMonth");
                        getSentMessages().add(message);
                    } catch (TelegramApiException e) {
                        if (((TelegramApiException) e).getCause().getLocalizedMessage().contains(
                                "message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message")) {
                            logger.info("Message not edited, no need.");
                        } else {
                            logger.error(((TelegramApiException) e).getLocalizedMessage(), e);
                        }
                    }
            }
        } catch (TelegramApiException e) {
            if (e.getMessage().equals("Error editing message reply markup")) {
                logger.info("1 message not changed.");
            } else {
                logger.error(((TelegramApiRequestException) e).getApiResponse(), e);
            }
        }
        if (message != null) {
            getSentMessages().add(message);
        }
    }

    @Override
    public String getProcessName() {
        return "Get sum";
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
