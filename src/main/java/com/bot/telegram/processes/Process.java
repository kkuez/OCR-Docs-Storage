package com.bot.telegram.processes;

import com.Main;
import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Process {

    public static final Logger logger = Main.getLogger();

    private ProgressReporter progressReporter;

    private Boolean hasStarted = false;

    private boolean deleteLater = false;

    private boolean awaitsInput = false;

    public abstract void performNextStep(String arg, Update update, Bot bot) throws TelegramApiException;

    public abstract String getProcessName();

    private List<Message> sentMessages = new ArrayList<>();

    private BackendFacade facade;

    public Process(ProgressReporter reporter, BackendFacade facade)
    {
        progressReporter = reporter;
        this.facade = facade;
    }

    private void clearButtons(Bot bot){
        int caughtMessages = 0;
        for(Message message : getSentMessages()){
            if(message != null){
                try {
                    bot.simpleEditMessage(message.getText(), message, KeyboardFactory.KeyBoardType.NoButtons, "");
                } catch (TelegramApiException e) {
                    if(e.getMessage().equals("Error editing message reply markup") || e.getMessage().equals("Error editing message text")){
                        caughtMessages++;
                    }else{
                        logger.error(((TelegramApiRequestException) e).getApiResponse(), e);
                    }
                }
            }
        }
        if(caughtMessages > 0){
            logger.info(caughtMessages + " messages caught.");
        }
    }

    public void close(Bot bot){
        clearButtons(bot);
        setDeleteLater(true);
    }

    String[] deserializeInput(Update update, Bot bot){
        String command = getCommandIfPossible(update, bot);
        String updateText = update.hasCallbackQuery() ? update.getCallbackQuery().getData() :  bot.getMassageFromUpdate(update).getText();
        String value = updateText.replace(command, "");


        //Normaly its command => Processstep, value => value. Sometimes there are "stepindependet" values to perform, these are set here.
        if(update.hasCallbackQuery()){
             updateText = update.getCallbackQuery().getData();
            if(updateText.startsWith("abort")){
                command = "abort";
                value = updateText.replace(command, "");
            }else{
            if(updateText.startsWith("remove")){
                command = "remove";
                value = updateText.replace(command, "");
            }else {
                if (updateText.startsWith("add")) {
                    command = "add";
                    value = updateText.replace(command, "");
                } else {
                    if (updateText.startsWith("done")) {
                        command = "done";
                    }
                }
            }
            }
        }
        return new String[]{command, value};
    }

    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap, Bot bot) {
        //Method only to setup stuff, like Bot in this case
        try {
            performNextStep(arg, update, bot);
        } catch (TelegramApiException e) {
            logger.error("Couldnt execute update.", e);
        }
    };

    //GETTER SETTER
    public boolean isAwaitsInput() {
        return awaitsInput;
    }

    public void setAwaitsInput(boolean awaitsInput) {
        this.awaitsInput = awaitsInput;
    }

    public synchronized List<Message> getSentMessages() {
        return sentMessages;
    }

    public boolean isDeleteLater() {
        return deleteLater;
    }

    public boolean getDeleteLater() {
        return deleteLater;
    }

    public void setDeleteLater(boolean deleteLater) {
        this.deleteLater = deleteLater;
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
