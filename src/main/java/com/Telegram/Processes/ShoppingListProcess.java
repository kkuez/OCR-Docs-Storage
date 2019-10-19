package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.*;

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
        Set<String> commandsWithLaterExecution = Set.of("Hinzufügen", "Löschen");
        if(action != null){
            item = BotUtil.getMassageFromUpdate(update).getText();
            if(action.equals("removeitem")){
                try{
                    item = update.getCallbackQuery().getData();
                    DBUtil.executeSQL("delete from ShoppingList where item='" +  item + "'");
                    getBot().getShoppingList().remove(item);
                    BotUtil.sendAnswerCallbackQuery(item + " gelöscht.", getBot(), false, update.getCallbackQuery());
                    BotUtil.sendMsg(item + " gelöscht.", getBot(), update, null, true, false);
                }catch (Exception e){
                    LogUtil.logError(null, e);
                }
            }
        }

        if(!commandsWithLaterExecution.contains(BotUtil.getMassageFromUpdate(update).getText())){
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
        BotUtil.sendMsg(listeBuilder.toString(), getBot(), update, null, true, false);
    }

    private void prepareForProcessing(Update update){
        Message message = null;
        switch (update.getMessage().getText()){
            case "Hinzufügen":
                message = BotUtil.sendMsg("Was soll hinzugefügt werden?", getBot(), update, KeyboardFactory.KeyBoardType.Abort, false, true);
                action = "add";
                break;
            case "Löschen":
                ReplyKeyboard shoppingListKeyboard = KeyboardFactory.getInlineKeyboardForList(DBUtil.getShoppingListFromDB());
                message = BotUtil.sendKeyboard("Was soll gelöscht werden?", getBot(), update, shoppingListKeyboard, false);
                action = "removeitem";
                break;
        }
        getSentMessages().add(message);
    }

    private void processInOneStep(String arg, Update update, Map<Integer, User> allowedUsersMap){
        String input = null;
        if(item != null){
            input = action + " " + item;
        }else{
            input = update.getMessage().getText();
        }
        String cmd = "";
        if(input.equals("Liste Löschen")){
            cmd = "removeall";
        }
        if(input.equals("Liste anzeigen")){
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
                BotUtil.sendMsg(arg + " hinzugefügt! :)", getBot(), update, null, true, false);
                close();
                break;
            case "getlist":
                sendShoppingList(update);
                break;
            case "removeall":
                DBUtil.executeSQL("Drop Table ShoppingList; create Table ShoppingList(item TEXT);");
                getBot().setShoppingList(new ArrayList<String>());
                BotUtil.sendMsg("Einkaufsliste gelöscht :)", getBot(), update, null, true, false);
                break;
        }
        setDeleteLater(true);
    }
    @Override
    public String getProcessName() {
        return "Shoppinglist Process";
    }

    //GETTER SETTER

}
