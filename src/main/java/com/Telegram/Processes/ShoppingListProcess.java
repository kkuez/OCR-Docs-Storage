package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

public class ShoppingListProcess extends Process{

    public ShoppingListProcess(Bot bot, Update update, ProgressReporter progressReporter){
        super(progressReporter);
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
                DBUtil.executeSQL("insert into ShoppingList(item) Values ('" + arg + "')");
                BotUtil.sendMsg(update.getMessage().getChatId() + "", arg + " hinzugefügt! :)", getBot());
                break;
            case "removeitem":
                try{
                    DBUtil.executeSQL("delete from ShoppingList where item='" +  getBot().getShoppingList().get(Integer.parseInt(arg)) + "'");
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
                DBUtil.executeSQL("Drop Table ShoppingList; create Table ShoppingList(item TEXT);");
                getBot().setShoppingList(new ArrayList<String>());
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Einkaufsliste gelöscht :)", getBot());
                break;
        }
        getBot().setBusy(false);
        getBot().process = null;
    }

    @Override
    public String getProcessName() {
        return "Shoppinglist Process";
    }
}
