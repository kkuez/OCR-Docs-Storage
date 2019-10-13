package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
                    DBUtil.executeSQL("update Documents set originalFile = '" + newOriginalFilePath + "' where originalFile = '" + document.getOriginFile().getAbsolutePath() + "'");
                    document.setOriginFile(newOriginalFilePath);
                    BotUtil.askBoolean("Endsumme " + bon.getSum() + "?", update, getBot(), true);
                    currentStep = Steps.isSum;
                    getBot().setBusy(false);
                }else{
                    if(arg.equals("Nee")) {
                        BotUtil.simpleEditMessage("Ok :)", getBot(), update, null);
                        setDeleteLater(true);
                    }else{
                        BotUtil.simpleEditMessage("Falsche eingabe...", getBot(), update, KeyboardFactory.KeyBoardType.Boolean);
                    }
                }
                break;

            case isSum:
                if(arg.equals("Japp")){
                    BotUtil.sendMsg("Ok :)",getBot(), update, null, true, false);
                    DBUtil.insertDocumentToDB(bon);
                    DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');" );
                    setDeleteLater(true);
                }else{
                    if(arg.equals("Nee")) {
                        BotUtil.sendMsg("Bitte richtige Summe eingeben:", getBot(), update, KeyboardFactory.KeyBoardType.Abort, false, true);
                        currentStep = Steps.EnterRightSum;
                    }else{
                        BotUtil.simpleEditMessage("Falsche eingabe...", getBot(), update, KeyboardFactory.KeyBoardType.Boolean);
                    }
                }
                break;
            case EnterRightSum:
                float sum = 0f;
                try {
                    sum = Float.parseFloat(arg.replace(",", "."));
                    bon.setSum(sum);
                    DBUtil.insertDocumentToDB(bon);
                    DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", 'Bon');" );
                    BotUtil.sendMsg("Ok, richtige Summe korrigiert :)", getBot(), update, null, false, false);
                    setDeleteLater(true);
                }catch (NumberFormatException e){
                    BotUtil.sendMsg("Die Zahl verstehe ich nicht :(", getBot(), update, KeyboardFactory.KeyBoardType.Abort, false, true);
                }
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
