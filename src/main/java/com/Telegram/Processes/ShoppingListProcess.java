package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ShoppingListProcess extends Process{

    String action = null;
    String item = null;

    public ShoppingListProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        this.setBot(bot);
        getBot().setBusy(true);
        performNextStep("asd", update,  allowedUsersMap);
    }
    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        //Terms in this set need more userinformation in a further step
        Set<String> commandsWithLaterExecution = Set.of("Hinzufügen", "Item Löschen");
        if(action != null){
            item = update.getMessage().getText();
        }

        if(!commandsWithLaterExecution.contains(update.getMessage().getText())){
            processInOneStep(arg, update, allowedUsersMap);
        }else{
            prepareForProcessing(update);
        }

        getBot().setBusy(false);
    }

    private void sendShoppingList(Update update){
        StringBuilder listeBuilder = new StringBuilder("Aktuelle Einkaufsliste:\n");
        for(int i = 0;i<getBot().getShoppingList().size();i++){
            listeBuilder.append( i + ": " + getBot().getShoppingList().get(i) + "\n");
        }
        BotUtil.sendMsg(update.getMessage().getChatId() + "", listeBuilder.toString(), getBot());
    }

    private void prepareForProcessing(Update update){
        switch (update.getMessage().getText()){
            case "Hinzufügen":
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Was soll hinzugefügt werden?", getBot());
                action = "add";
                break;
            case "Item Löschen":
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Nummer auf der Liste soll gelöscht werden?", getBot());
                action = "removeitem";
                break;
        }
    }
    private void processInOneStep(String arg, Update update, Map<Integer, User> allowedUsersMap){
        String input = null;
        if(item != null){
            input = action + " " + item;
        }else{
            input = update.getMessage().getText();
        }
        String cmd = "";
        if(input.equals("Ganze Liste Löschen")){
            cmd = "removeall";
        }
        if(input.equals("Einkaufsliste anzeigen")){
            cmd = "getlist";
        }

        if(input.contains("add") || input.contains("removeitem")){
            cmd = input.substring(0, input.indexOf(" ")).toLowerCase();
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
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", getBot().getShoppingList().get(Integer.parseInt(arg)) + " gelöscht.", getBot());
                }catch (Exception e){
                    LogUtil.logError(null, e);
                    BotUtil.sendMsg(update.getMessage().getChatId() + "", arg + " nicht gelöscht. Hast du eine Zahl aus der Liste angegeben? (/getList)", getBot());
                }
                break;
            case "getlist":
                sendShoppingList(update);
                break;
            case "removeall":
                DBUtil.executeSQL("Drop Table ShoppingList; create Table ShoppingList(item TEXT);");
                getBot().setShoppingList(new ArrayList<String>());
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Einkaufsliste gelöscht :)", getBot());
                break;
        }
        setDeleteLater(true);
    }
    @Override
    public String getProcessName() {
        return "Shoppinglist Process";
    }
}
