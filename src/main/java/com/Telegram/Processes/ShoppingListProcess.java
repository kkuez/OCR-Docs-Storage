package com.Telegram.Processes;

import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

public class ShoppingListProcess extends Process{

    public ShoppingListProcess(Bot bot, Update update){
        this.setBot(bot);
        getBot().setBusy(true);
        performNextStep("asd", update);
    }
    @Override
    public void performNextStep(String arg, Update update) {
        String input = update.getMessage().getText();
        String cmd = "";
        if(input.contains(" ")){
            cmd = input.substring(0, update.getMessage().getText().indexOf(" ")).toLowerCase();
        }else{
            cmd = input.toLowerCase();
        }

         arg = input.substring(input.indexOf(" ") + 1);
        switch (cmd){
            case "add":
                getBot().getShoppingList().add(arg);
                BotUtil.sendMsg(update.getMessage().getChatId() + "", arg + " hinzugefügt! :)", getBot());
                break;
            case "removeitem":
                try{
                    getBot().getShoppingList().remove(Integer.parseInt(arg));
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", arg + " gelöscht.", getBot());
                }catch (Exception e){
                    LogUtil.logError(null, e);
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", arg + " nicht gelöscht. Hast du eine Zahl aus der Liste angegeben? (/getList)", getBot());
                }


                break;
            case "getlist":
                StringBuilder listeBuilder = new StringBuilder("Aktuelle Einkaufsliste:\n");
                for(int i = 0;i<getBot().getShoppingList().size();i++){
                    listeBuilder.append( i + ": " + getBot().getShoppingList().get(i) + "\n");
                }
                BotUtil.sendMsg(update.getMessage().getChatId() + "", listeBuilder.toString(), getBot());
                break;
            case "removeall":
                getBot().setShoppingList(new ArrayList<String>());
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Einkaufsliste gelöscht :)", getBot());
                break;
        }
        getBot().setBusy(false);
        getBot().process = null;
    }
}
