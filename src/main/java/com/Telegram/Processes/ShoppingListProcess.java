package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class ShoppingListProcess extends Process{

    private AWAITING_INPUT status = null;

    User user;


    public ShoppingListProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        user = allowedUsersMap.get(getBot().getMassageFromUpdate(update).getFrom().getId());
        this.setBot(bot);
        user.setBusy(true);
        performNextStep("-", update,  allowedUsersMap);
    }

    private void sendShoppingList(Update update){
        StringBuilder listeBuilder = new StringBuilder("Aktuelle Einkaufsliste:\n");
        for(int i = 0;i<getBot().getShoppingList().size();i++){
            listeBuilder.append( i + ": " + getBot().getShoppingList().get(i) + "\n");
        }
        getBot().sendMsg(listeBuilder.toString(), update, null, false, false);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        String[] commandValue = deserializeInput(update);
        Message message = null;
        switch (commandValue[0]){
            case "add":
                String item = commandValue[1];
                getBot().getShoppingList().add(item);
                DBUtil.executeSQL("insert into ShoppingList(item) Values ('" + item + "')");
                if(update.hasCallbackQuery()){
                    try {
                        getBot().sendAnswerCallbackQuery(item + " hinzugefügt! :) Noch was?", false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }else{
                    message = getBot().sendMsg(item + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done, false, true);
                }
                getSentMessages().add(message);
                break;
            case "remove":
                try{
                    item = commandValue[1];
                    DBUtil.executeSQL("delete from ShoppingList where item='" +  item + "'");
                    getBot().getShoppingList().remove(item);
                    getBot().sendAnswerCallbackQuery(item + " gelöscht. Nochwas?", false, update.getCallbackQuery());
                    getBot().simpleEditMessage(item + " gelöscht. Nochwas?", getBot().getMassageFromUpdate(update), KeyboardFactory.KeyBoardType.ShoppingList_Current, "remove");
                }catch (Exception e){
                    LogUtil.logError(null, e);
                }
                break;
            case "done":
                getBot().sendMsg("Ok :)", update, null, false, false);
                close();
                break;
            case "Liste anzeigen":
                sendShoppingList(update);
                close();
                break;
            case "Liste Löschen":
                DBUtil.executeSQL("Drop Table ShoppingList; create Table ShoppingList(item TEXT);");
                getBot().setShoppingList(new ArrayList<String>());
                getBot().sendMsg("Einkaufsliste gelöscht :)", update, null, false, false);
                close();
                break;
            case "Löschen":
                ReplyKeyboard shoppingListKeyboard = KeyboardFactory.getInlineKeyboardForList(DBUtil.getShoppingListFromDB(), "remove");
                message = getBot().sendKeyboard("Was soll gelöscht werden?", update, shoppingListKeyboard, false);
                break;
            case "Hinzufügen":
                message = getBot().sendMsg("Was soll hinzugefügt werden?", update, KeyboardFactory.KeyBoardType.ShoppingList_Add, false, true);
                status = AWAITING_INPUT.add;
                break;
            case"Standardliste anzeigen":
                message = getBot().simpleEditMessage("Standardliste:", update, KeyboardFactory.KeyBoardType.StandardList_Current, "add");
                break;
        }
        user.setBusy(false);
        if(message != null){
            getSentMessages().add(message);
        }
    }

    @Override
    public String getProcessName() {
        return "Shoppinglist Process";
    }

    @Override
    public String getCommandIfPossible(Update update) {
        if(update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("Standardliste anzeigen")){
            return "Standardliste anzeigen";
        }else{
        if(status == AWAITING_INPUT.add){
            return "add";
        }else{
            if(update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("remove")){
                return "remove";
            }
        }
        }

        return !update.hasCallbackQuery() ? update.getMessage().getText() : "";
    }

    enum AWAITING_INPUT{
        add
    }

    //GETTER SETTER

}
