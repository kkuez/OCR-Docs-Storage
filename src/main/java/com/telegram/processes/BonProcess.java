package com.telegram.processes;

import com.controller.reporter.ProgressReporter;
import com.ObjectHub;
import com.objectTemplates.Bon;
import com.objectTemplates.Document;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.telegram.KeyboardFactory;
import com.utils.DBUtil;

import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BonProcess extends Process {

    private Steps currentStep;

    private Bon bon;

    private Document document;

    public BonProcess(Bot bot, ProgressReporter progressReporter){
        super(progressReporter);
        setBot(bot);
        currentStep = Steps.enterBon;
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap){
        String[] commandValue = deserializeInput(update);
        Message message = null;
        User user = getBot().getNonBotUserFromUpdate(update);
        try {
            switch (commandValue[0]) {
                case "abort":
                    getBot().abortProcess(update);
                    break;
                case "Start":
                    if (commandValue[1].equals("confirm")) {
                        user.setBusy(true);
                        //In Bonfolder kompieren nachdem der User bestätigt hat dass Dok ein Bon ist.
                        File newOriginalFilePath = new File(ObjectHub.getInstance().getArchiver().getBonFolder(), document.getOriginalFileName());
                        try {
                            FileUtils.copyFile(document.getOriginFile(), newOriginalFilePath);
                        } catch (IOException e) {
                            logger.error(document.getOriginFile().getAbsolutePath(), e);
                        }
                        FileUtils.deleteQuietly(document.getOriginFile());
                        DBUtil.executeSQL("update Documents set originalFile = '" + newOriginalFilePath + "' where originalFile = '" + document.getOriginFile().getAbsolutePath() + "'");
                        document.setOriginFile(newOriginalFilePath);
                        message = getBot().askBoolean("Endsumme " + bon.getSum() + "?", update, true);
                        currentStep = Steps.isSum;
                        user.setBusy(false);
                    } else {
                        if (commandValue[1].equals("deny")) {
                            message = getBot().simpleEditMessage("Ok :)", update, KeyboardFactory.KeyBoardType.NoButtons);
                            close();
                        } else {
                            message = getBot().simpleEditMessage("Falsche eingabe...", update, KeyboardFactory.KeyBoardType.Boolean);
                        }
                    }
                    break;
                case "isSum":
                    if (commandValue[1].equals("confirm")) {
                        getBot().sendMsg("Ok :)", update, null, true, false);
                        DBUtil.insertDocumentToDB(bon);
                        DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');");
                        setDeleteLater(true);
                        close();
                    } else {
                        if (commandValue[1].equals("deny")) {
                            message = getBot().sendMsg("Bitte richtige Summe eingeben:", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                            currentStep = Steps.EnterRightSum;
                        } else {
                            message = getBot().simpleEditMessage("Falsche eingabe...", update, KeyboardFactory.KeyBoardType.Boolean);
                        }
                    }
                    break;
                case "EnterRightSum":
                    float sum = 0f;
                    try {
                        sum = Float.parseFloat(commandValue[1].replace(",", "."));
                        bon.setSum(sum);
                        DBUtil.insertDocumentToDB(bon);
                        DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');");
                        getBot().sendMsg("Ok, richtige Summe korrigiert :)", update, null, false, false);
                        close();
                    } catch (NumberFormatException e) {
                        message = getBot().sendMsg("Die Zahl verstehe ich nicht :(", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                    }
                    break;
                default:
                    if (currentStep == Steps.enterBon) {
                        currentStep = Steps.Start;
                        this.document = DBUtil.getDocumentForID(bon.getBelongsToDocument());
                        performNextStep("Start", update, allowedUsersMap);
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
    public String getCommandIfPossible(Update update) {
        return currentStep.toString();
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
