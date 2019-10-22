package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.Item;
import com.Telegram.KeyboardFactory;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class ShoppingListProcess extends Process{

    String action = null;
    String item = null;
    int index = 0;
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
            item = getBot().getMassageFromUpdate(update).getText();
            if(action.equals("removeitem")){
                try{
                    item = update.getCallbackQuery().getData();
                    DBUtil.executeSQL("delete from ShoppingList where item='" +  item + "'");
                    getBot().getShoppingList().remove(item);
                    getBot().sendAnswerCallbackQuery(item + " gelöscht.", false, update.getCallbackQuery());
                        getBot().simpleEditMessage(item + " gelöscht. Nochwas?", getBot().getMassageFromUpdate(update), KeyboardFactory.KeyBoardType.ShoppingList_Current);
                    }catch (Exception e){
                    LogUtil.logError(null, e);
                }
            }
        }
        Message message = getBot().getMassageFromUpdate(update);
        if(!message.hasText() || !commandsWithLaterExecution.contains(message.getText())){
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
        getBot().sendMsg(listeBuilder.toString(), update, null, false, false);
    }

    private void prepareForProcessing(Update update){
        Message message = null;
        switch (update.getMessage().getText()){
            case "Hinzufügen":
                message = getBot().sendMsg("Was soll hinzugefügt werden?", update, KeyboardFactory.KeyBoardType.StandardList_Abort, false, true);
                action = "add";
                break;
            case "Löschen":
                ReplyKeyboard shoppingListKeyboard = KeyboardFactory.getInlineKeyboardForList(DBUtil.getShoppingListFromDB());
                message = getBot().sendKeyboard("Was soll gelöscht werden?", update, shoppingListKeyboard, false);
                action = "removeitem";
                break;
        }
        getSentMessages().add(message);
    }

    private void processInOneStep(String arg, Update update, Map<Integer, User> allowedUsersMap){
        String input = null;
        String cmd = arg;
        if(arg.equals("done")){
            input = "done";
        }else{
            if(arg.equals("select")){
                item = update.getCallbackQuery().getMessage().getCaption();
                input = action + " " + item;
            }else{
            if(item != null){
                input = action + " " + item;
            }else{
                input = getBot().getMassageFromUpdate(update).getText();
            }}}

        switch (input){
            case "Liste Löschen":
                DBUtil.executeSQL("Drop Table ShoppingList; create Table ShoppingList(item TEXT);");
                getBot().setShoppingList(new ArrayList<String>());
                getBot().sendMsg("Einkaufsliste gelöscht :)", update, null, false, false);
                close();
                break;
            case "done":
                getBot().sendMsg("Ok :)", update, null, false, false);
                close();
                break;
            case "Liste anzeigen":
                sendShoppingList(update);
                close();
                break;
            default:
                if((input.contains("add") || input.contains("removeitem"))){
                    cmd = input.substring(0, input.indexOf(" ")).toLowerCase();
                }
                break;
        }

        arg = input.substring(input.indexOf(" ") + 1);
        Message message = getBot().getMassageFromUpdate(update);
        List<Item> standardList = DBUtil.getStandardListFromDB();
        Item itemFromList = null;
        switch (cmd){
            case "select":
                arg = update.getCallbackQuery().getMessage().getCaption();
            case "add":

                if(update.hasCallbackQuery() && update.getCallbackQuery().getData().equals("Standardliste anzeigen")){
                    message = getBot().sendOrEditSLIDESHOWMESSAGE(standardList.size() == 0 ? "-leer-" : standardList.get(0).getName(), standardList.size() == 0 ? null : standardList.get(0), update);
                    try {
                        getBot().sendAnswerCallbackQuery("Standardliste anzeigen", false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        LogUtil.logError("", e);
                    }
                }else {
                    getBot().getShoppingList().add(arg);
                    DBUtil.executeSQL("insert into ShoppingList(item) Values ('" + arg + "')");
                    if (update.hasCallbackQuery()) {

                        try {
                            getBot().sendAnswerCallbackQuery(arg + " hinzugefügt! :) Noch was?", false, update.getCallbackQuery());
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        message = getBot().sendMsg(arg + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done, false, true);
                    }
                }
                getSentMessages().add(message);
                break;
            case "<<":
                index = 0;
                getBot().sendOrEditSLIDESHOWMESSAGE(message.getText(), standardList.get(index), update);
                itemFromList = standardList.get(index);
                break;
            case "<":
                index = index != 0 ? index - 1 : 0;
                getBot().sendOrEditSLIDESHOWMESSAGE(message.getText(), standardList.get(index), update);
                itemFromList = standardList.get(index);
                break;
            case ">":
                index = index == standardList.size() - 1 ? standardList.size() - 1 : index + 1;
                getBot().sendOrEditSLIDESHOWMESSAGE(message.getText(), standardList.get(index), update);
                itemFromList = standardList.get(index);
                break;
            case ">>":
                index = standardList.size() - 1;
                getBot().sendOrEditSLIDESHOWMESSAGE(message.getText(), standardList.get(index), update);
                itemFromList = standardList.get(index);
                break;
        }
    }
    @Override
    public String getProcessName() {
        return "Shoppinglist Process";
    }

    //GETTER SETTER

}
