package com.Misc.Processes;

import com.ObjectTemplates.Bon;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class BonProcess extends Process {

    private Steps currentStep;

    Bot bot;

    private Bon bon;

    public BonProcess(Bon bon, Bot bot){
        this.bon = bon;
        this.bot = bot;
        currentStep = Steps.Start;
    }

    @Override
    public void performNextStep(String arg, Update update) {
        switch(currentStep){
            case Start:
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
                bon.setSum(Float.parseFloat(arg));
                DBUtil.insertDocumentToDB(bon);
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Ok, richtige Summe korrigiert :)",bot);
                Bot.process = null;
                break;
        }
    }

    enum Steps{
        Start, isSum, EnterRightSum
    }


}
