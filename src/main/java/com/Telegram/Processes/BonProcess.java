package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;

public class BonProcess extends Process {

    private Steps currentStep;

    private Bon bon;

    public BonProcess(Bon bon, Bot bot, Document document, ProgressReporter progressReporter){
        super(progressReporter);
        this.bon = bon;
        setBot(bot);
        this.document = document;
        currentStep = Steps.Start;
    }

    @Override
    public void performNextStep(String arg, Update update) {
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
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", "Ok :)",getBot());
                    getBot().process = null;
                }
                break;

            case isSum:
                if(arg.equals("Japp")){
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", "OK :)",getBot());
                    DBUtil.insertDocumentToDB(bon);
                    DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');" );
                    getBot().process = null;
                }else{
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", "Bitte richtige Summe eingeben:",getBot());
                    currentStep = Steps.EnterRightSum;
                }
                break;

            case EnterRightSum:
                bon.setSum(Float.parseFloat(arg.replace(",", ".")));
                DBUtil.insertDocumentToDB(bon);
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Ok, richtige Summe korrigiert :)",getBot());
                getBot().process = null;
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
