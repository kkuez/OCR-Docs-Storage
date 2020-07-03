package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.backend.ObjectHub;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;

import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class BonProcess extends Process {

    private Steps currentStep;

    private Bon bon;

    private Document document;

    private static Set<String> commands = Set.of(
            "Start",
            "isSum",
            "EnterRightSum");
    public BonProcess(ProgressReporter progressReporter, BackendFacade facade){
        super(progressReporter, facade);
        currentStep = Steps.enterBon;
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot){
        String[] commandValue = deserializeInput(update, bot);
        Message message = null;
        User user = bot.getNonBotUserFromUpdate(update);
        try {
            switch (commandValue[0]) {
                case "abort":
                    bot.abortProcess(update);
                    break;
                case "Start":
                    if (commandValue[1].equals("confirm")) {
                        user.setBusy(true);
                        //In Bonfolder kopieren nachdem der User bestätigt hat dass Dok ein Bon ist.
                        File newOriginalFile = new File(ObjectHub.getInstance().getArchiver().getBonFolder(), bon.getOriginalFileName());
                        try {
                            FileUtils.copyFile(bon.getOriginFile(), newOriginalFile);
                        } catch (IOException e) {
                            logger.error(bon.getOriginFile().getAbsolutePath(), e);
                        }
                        FileUtils.deleteQuietly(bon.getOriginFile());
                        bon.setOriginFile(newOriginalFile);
                        getFacade().updateDocument(bon);
                        message = bot.askBoolean("Endsumme " + bon.getSum() + "?", update, true);
                        currentStep = Steps.isSum;
                        user.setBusy(false);
                    } else {
                        if (commandValue[1].equals("deny")) {
                            message = bot.simpleEditMessage("Ok :)", update, KeyboardFactory.KeyBoardType.NoButtons);
                            close(bot);
                        } else {
                            message = bot.simpleEditMessage("Falsche eingabe...", update, KeyboardFactory.KeyBoardType.Boolean);
                        }
                    }
                    break;
                case "isSum":
                    if (commandValue[1].equals("confirm")) {
                        bot.sendMsg("Ok :)", update, null, true, false);
                        getFacade().insertDocument(bon);
                        getFacade().insertTag(bon.getId(), "Bon");
                        setDeleteLater(true);
                        close(bot);
                    } else {
                        if (commandValue[1].equals("deny")) {
                            message = bot.sendMsg("Bitte richtige Summe eingeben:", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                            currentStep = Steps.EnterRightSum;
                        } else {
                            message = bot.simpleEditMessage("Falsche eingabe...", update, KeyboardFactory.KeyBoardType.Boolean);
                        }
                    }
                    break;
                case "EnterRightSum":
                    float sum = 0f;
                    try {
                        sum = Float.parseFloat(commandValue[1].replace(',', '.'));
                        bon.setSum(sum);
                        getFacade().insertDocument(bon);
                        getFacade().insertTag(bon.getId(), "B");
                        getFacade().insertTag(bon.getId(), "Bon");
                        bot.sendMsg("Ok, richtige Summe korrigiert :)", update, null, false, false);
                        close(bot);
                    } catch (NumberFormatException e) {
                        message = bot.sendMsg("Die Zahl verstehe ich nicht :(", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                    }
                    break;
                default:
                    if (currentStep == Steps.enterBon) {
                        currentStep = Steps.Start;
                        performNextStep("Start", update, bot);
                    }
                    break;
            }
        } catch (TelegramApiException e) {
            if(e.getMessage().equals("Error editing message reply markup")){
                logger.info("1 message not changed.");
            }else{
                logger.error(((TelegramApiRequestException) e).getApiResponse(), e);
            }
        }
        if(message != null){
            getSentMessages().add(message);
        }
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        return currentStep.toString();
    }

    @Override
    public boolean hasCommand(String cmd) {
        return false;
    }

    @Override
    public String getProcessName() {
        return "Bon-Process";
    }

    private enum Steps{
        Start, enterBon, isSum, EnterRightSum
    }

    //GETTER SETTER
    public Steps getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Steps currentStep) {
        this.currentStep = currentStep;
    }

    public Bon getBon() {
        return bon;
    }

    public void setBon(Bon bon) {
        this.bon = bon;
    }


    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

}
