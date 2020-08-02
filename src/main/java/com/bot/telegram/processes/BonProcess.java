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
import java.util.Set;

public class BonProcess extends Process {

    private Steps currentStep;

    private Bon bon;

    private Document document;

    private static Set<String> commands = Set.of(
            "Start",
            "isSum",
            "Bon-Optionen",
            "Bon eingeben",
            "EnterRightSum");

    public BonProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
        currentStep = Steps.enterBon;
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        User user = bot.getNonBotUserFromUpdate(update);
        String[] commandValue = deserializeInput(update, bot);
        Message message = null;
        try {
            switch (commandValue[0]) {
                case "isBon":
                    if(commandValue[1].equals("confirm")) {
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
                        message = bot.askBoolean("Endsumme " + bon.getSum() + "?", update, true, "isSum");
                        currentStep = Steps.isSum;
                        user.setBusy(false);
                    }
                    if(commandValue[1].equals("deny")) {
                        message = bot.simpleEditMessage("Ok :)", update, KeyboardFactory.KeyBoardType.NoButtons, null);
                        reset(bot, user);
                    }
                break;
                case "isSum":
                    if(commandValue[1].equals("confirm")) {
                        bot.sendMsg("Ok :)", update, null, true, false);
                        getFacade().insertDocument(bon);
                        getFacade().insertTag(bon.getId(), "Bon");
                        reset(bot, user);
                    }
                    if(commandValue[1].equals("deny")) {
                        message = bot.sendMsg("Bitte richtige Summe eingeben:", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                        currentStep = Steps.EnterRightSum;
                    } else {
                        message = bot.simpleEditMessage("Falsche eingabe...", update, KeyboardFactory.KeyBoardType.Boolean);
                    }
                    break;
                case "Bon eingeben":
                    bot.sendMsg("Bitte lad jetzt den Bon hoch.", update, KeyboardFactory.KeyBoardType.NoButtons, true, false);
                    break;
                case "abort":
                    bot.abortProcess(update);
                    break;
                case "EnterRightSum":

                    break;
                case "Bon-Optionen":
                    bot.sendKeyboard("Was willst du tun?", update, KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Bons,
                            false, false, null, getFacade()), false);
                    break;
                default:
                    if (currentStep == Steps.enterBon) {
                        currentStep = Steps.Start;
                        performNextStep("Start", update, bot);
                    } else {
                        if (currentStep == Steps.EnterRightSum) {
                            float sum;
                            try {
                                sum = Float.parseFloat(commandValue[1].replace(',', '.'));
                                bon.setSum(sum);
                                getFacade().insertDocument(bon);
                                getFacade().insertTag(bon.getId(), "B");
                                getFacade().insertTag(bon.getId(), "Bon");
                                bot.sendMsg("Ok, richtige Summe korrigiert :)", update, null, false, false);
                                reset(bot, user);
                            } catch (NumberFormatException e) {
                                message = bot.sendMsg("Die Zahl verstehe ich nicht :(", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                            }
                        }
                    }
                    break;
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
    public void reset(Bot bot, User user) {
        currentStep = Steps.enterBon;
        super.reset(bot, user);
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        String text = "";
        if (!update.hasCallbackQuery()) {
            text = update.getMessage().getText();
        }

        switch (text) {
            case "Bon eingeben":
                return text;
            default:
                return text;
        }
    }

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }

    @Override
    public String getProcessName() {
        return "Bon-Process";
    }

    private enum Steps {
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
