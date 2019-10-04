package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BonProcess extends Process {

    private Steps currentStep;

    private Bon bon;

    public BonProcess(Bon bon, Bot bot, Document document, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        this.bon = bon;
        setBot(bot);
        this.document = document;
        currentStep = Steps.Start;
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        switch(currentStep){
            case Start:
                if(arg.equals("Japp")) {
                    getBot().setBusy(true);
                    //In Bonfolder kompieren nachdem der User bestätigt hat dass Dok ein Bon ist.
                    File newOriginalFilePath = new File(ObjectHub.getInstance().getArchiver().getBonFolder(), document.getOriginalFileName());
                    try {
                        FileUtils.copyFile(document.getOriginFile(), newOriginalFilePath);
                    } catch (IOException e) {
                        LogUtil.logError(document.getOriginFile().getAbsolutePath(), e);
                    }
                    FileUtils.deleteQuietly(document.getOriginFile());
                    document.setOriginFile(newOriginalFilePath);
                    BotUtil.askBoolean("Endsumme " + bon.getSum() + "?", update, getBot());
                    currentStep = Steps.isSum;
                    getBot().setBusy(false);
                }else{
                    BotUtil.sendMsg("Ok :)",getBot(), update.getMessage(), null, true, false);
                    setDeleteLater(true);
                }
                break;

            case isSum:
                if(arg.equals("Japp")){
                    BotUtil.sendMsg("Ok :)",getBot(), update.getMessage(), null, true, false);
                    DBUtil.insertDocumentToDB(bon);
                    DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');" );
                    setDeleteLater(true);
                }else{
                    BotUtil.sendMsg("Bitte richtige Summe eingeben:",getBot(), update.getMessage(), null, true, false);
                    currentStep = Steps.EnterRightSum;
                }
                break;

            case EnterRightSum:
                bon.setSum(Float.parseFloat(arg.replace(",", ".")));
                DBUtil.insertDocumentToDB(bon);
                DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');" );
                BotUtil.sendMsg("Ok, richtige Summe korrigiert :)",getBot(), update.getMessage(), null, true, false);
                setDeleteLater(true);
                break;
        }
    }

    @Override
    public String getProcessName() {
        return "Bon-Process";
    }

    private enum Steps{
        Start, isSum, EnterRightSum
    }
}
