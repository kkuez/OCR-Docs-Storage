package com.Misc.Processes;

import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;

public class BonProcess extends Process {

    private Steps currentStep;

    Bot bot;

    private Bon bon;

    public BonProcess(Bon bon, Bot bot, Document document){
        this.bon = bon;
        this.bot = bot;
        this.document = document;
        currentStep = Steps.Start;
    }

    @Override
    public void performNextStep(String arg, Update update) {
        switch(currentStep){
            case Start:
                //In Bonfolder kompieren nachdem der User bestätigt hat dass Dok ein Bon ist.
                File newOriginalFilePath = new File(ObjectHub.getInstance().getArchiver().getBonFolder(), document.getOriginalFileName());
                try {
                    FileUtils.copyFile(document.getOriginFile(), newOriginalFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileUtils.deleteQuietly(document.getOriginFile());
                document.setOriginFile(newOriginalFilePath);
                BotUtil.askBoolean("Endsumme " + bon.getSum() + "?", update, bot);
                currentStep = Steps.isSum;
                break;

            case isSum:
                if(arg.equals("Japp")){
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", "OK :)",bot);
                    DBUtil.insertDocumentToDB(bon);
                    Bot.process = null;
                }else{
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", "Bitte richtige Summe eingeben:",bot);
                    currentStep = Steps.EnterRightSum;
                }
                break;

            case EnterRightSum:
                bon.setSum(Float.parseFloat(arg.replace(",", ".")));
                DBUtil.insertDocumentToDB(bon);
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Ok, richtige Summe korrigiert :)",bot);
                Bot.process = null;
                break;
        }
    }

    private enum Steps{
        Start, isSum, EnterRightSum
    }
}
