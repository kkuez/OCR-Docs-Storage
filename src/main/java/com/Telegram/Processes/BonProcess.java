package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BonProcess extends Process {

    private Steps currentStep;

    private Bon bon;

    public BonProcess(Bot bot, ProgressReporter progressReporter){
        super(progressReporter);
        setBot(bot);
        currentStep = Steps.enterBon;
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        String[] commandValue = deserializeInput(update);
        Message message = null;
        User user = getBot().getNonBotUserFromUpdate(update);
        switch (commandValue[0]){
            case "abort":
                getBot().abortProcess(update);
                break;
            case "Start":
                if(commandValue[1].equals("confirm")) {
                    user.setBusy(true);
                    //In Bonfolder kompieren nachdem der User bestätigt hat dass Dok ein Bon ist.
                    File newOriginalFilePath = new File(ObjectHub.getInstance().getArchiver().getBonFolder(), document.getOriginalFileName());
                    try {
                        FileUtils.copyFile(document.getOriginFile(), newOriginalFilePath);
                    } catch (IOException e) {
                        LogUtil.logError(document.getOriginFile().getAbsolutePath(), e);
                    }
                    FileUtils.deleteQuietly(document.getOriginFile());
                    DBUtil.executeSQL("update Documents set originalFile = '" + newOriginalFilePath + "' where originalFile = '" + document.getOriginFile().getAbsolutePath() + "'");
                    document.setOriginFile(newOriginalFilePath);
                    message = getBot().askBoolean("Endsumme " + bon.getSum() + "?", update,  true);
                    currentStep = Steps.isSum;
                    user.setBusy(false);
                }else{
                    if(commandValue[1].equals("deny")) {
                        getBot().simpleEditMessage("Ok :)", update, null);
                        setDeleteLater(true);
                    }else{
                        message = getBot().simpleEditMessage("Falsche eingabe...", update, KeyboardFactory.KeyBoardType.Boolean);
                    }
                }
                break;
            case "isSum":
                if(commandValue[1].equals("confirm")){
                    getBot().sendMsg("Ok :)",update, null, true, false);
                    DBUtil.insertDocumentToDB(bon);
                    DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');" );
                    setDeleteLater(true);
                    close();
                }else{
                    if(commandValue[1].equals("deny")) {
                        message = getBot().sendMsg("Bitte richtige Summe eingeben:", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                        currentStep = Steps.EnterRightSum;
                    }else{
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
                    DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');" );
                    getBot().sendMsg("Ok, richtige Summe korrigiert :)", update, null, false, false);
                    close();
                }catch (NumberFormatException e){
                    message = getBot().sendMsg("Die Zahl verstehe ich nicht :(", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                }
                break;
                default:
                    if(currentStep == Steps.enterBon){
                        currentStep = Steps.Start;
                        this.document = DBUtil.getDocumentForID(bon.getBelongsToDocument());
                        performNextStep("Start", update, allowedUsersMap);
                    }
                    break;
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

}
