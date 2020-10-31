package com.bot.telegram.processes;

import com.StartUp;
import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.reporter.ProgressReporter;
import com.objectTemplates.User;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Process {

    public static final Logger logger = StartUp.getLogger();

    private ProgressReporter progressReporter;

    private Boolean hasStarted = false;

    private boolean awaitsInput = false;

    public abstract void performNextStep(String arg, Update update, Bot bot) throws TelegramApiException;

    public abstract String getProcessName();

    private List<Message> sentMessages = new ArrayList<>();

    private BackendFacade facade;

    private final static Set<String> generalCommands = Set.of("abort", "remove", "add", "done", "confirm", "deny");

    public Process(ProgressReporter reporter, BackendFacade facade) {
        progressReporter = reporter;
        this.facade = facade;
    }

    private void clearButtons(Bot bot) {
        int caughtMessages = 0;
        for (Message message : getSentMessages()) {
            if (message != null) {
                try {
                    bot.simpleEditMessage(message.getText(), message, KeyboardFactory.KeyBoardType.NoButtons, "");
                } catch (TelegramApiException e) {
                    if (e.getMessage().equals("Error editing message reply markup")
                            || e.getMessage().equals("Error editing message text")) {
                        caughtMessages++;
                    } else {
                        logger.error(((TelegramApiRequestException) e).getApiResponse(), e);
                    }
                }
            }
        }
        if (caughtMessages > 0) {
            logger.info(caughtMessages + " messages caught.");
        }
    }

    public void reset(Bot bot, User user) {
        clearButtons(bot);
        user.setProcess(null);
    }

    String[] deserializeInput(Update update, Bot bot) {
        String command;
        String updateText;
        String value;

        // Normally its command => Processstep, value => value. Sometimes there are "stepindependet" values to perform,
        // these are set here.
        if (update.hasCallbackQuery()) {
            updateText = update.getCallbackQuery().getData();
        } else {
            updateText = bot.getMassageFromUpdate(update).getText();
        }
        if (updateText.contains(KeyboardFactory.DIVIDER)) {
            command = updateText.split(";")[0];
            value = updateText.split(";")[1];
        } else {
            command = value = updateText;
        }

        return new String[] { command, value };
    }

    protected String parseValue(String updateText) {
        return updateText.contains(KeyboardFactory.DIVIDER) ? updateText.split(KeyboardFactory.DIVIDER)[1] : updateText;
    }

    // GETTER SETTER
    public boolean isAwaitsInput() {
        return awaitsInput;
    }

    public void setAwaitsInput(boolean awaitsInput) {
        this.awaitsInput = awaitsInput;
    }

    public synchronized List<Message> getSentMessages() {
        return sentMessages;
    }

    public ProgressReporter getProgressReporter() {
        return progressReporter;
    }

    public void setProgressReporter(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }

    public BackendFacade getFacade() {
        return facade;
    }

    public Boolean getHasStarted() {
        return hasStarted;
    }

    public void setHasStarted(Boolean hasStarted) {
        this.hasStarted = hasStarted;
    }

    public abstract String getCommandIfPossible(Update update, Bot bot);

    public abstract boolean hasCommand(String cmd);

}
