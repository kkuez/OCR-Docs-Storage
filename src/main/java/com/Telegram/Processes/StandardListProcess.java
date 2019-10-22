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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StandardListProcess extends Process {

    AWAITING_INPUT status = null;

    public StandardListProcess(ProgressReporter progressReporter, Bot bot, Update update, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        this.setBot(bot);
        getBot().setBusy(true);
        performNextStep("-", update,  allowedUsersMap);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        String[] commandValue = deserializeInput(update);
        Message message = null;
        switch (commandValue[0]){
            case "add":
                String item = commandValue[1];
                DBUtil.executeSQL("insert into StandardList(item) Values ('" + item + "')");
                message = getBot().sendMsg(item + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done, false, true);
                getSentMessages().add(message);
                break;
            case "remove":
                try{
                    item = commandValue[1];
                    DBUtil.executeSQL("delete from StandardList where item='" +  item + "'");
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
                sendStandardList(update);
                close();
                break;
            case "Liste Löschen":
                DBUtil.executeSQL("Drop Table StandardList; create Table StandardList(item TEXT);");
                getBot().setShoppingList(new ArrayList<String>());
                getBot().sendMsg("Standardliste gelöscht :)", update, null, false, false);
                close();
                break;
            case "Löschen":
                ReplyKeyboard standardListKeyboard = KeyboardFactory.getInlineKeyboardForList(DBUtil.getStandardListFromDB(), "remove");
                message = getBot().sendKeyboard("Was soll gelöscht werden?", update, standardListKeyboard, false);
                break;
            case "Hinzufügen":
                message = getBot().sendMsg("Was soll hinzugefügt werden?", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                status = AWAITING_INPUT.add;
                break;
        }
        getBot().setBusy(false);
        if(message != null){
            getSentMessages().add(message);
        }
    }

    private void sendStandardList(Update update) {
        StringBuilder listeBuilder = new StringBuilder("Aktuelle Standardliste:\n");
        List<String> standardList = DBUtil.getStandardListFromDB();
        for(int i = 0;i<standardList.size();i++){
            listeBuilder.append( i + ": " + standardList.get(i) + "\n");
        }
        getBot().sendMsg(listeBuilder.toString(), update, null, false, false);
    }

    @Override
    public String getProcessName() {
        return null;
    }

    @Override
    public String getCommandIfPossible(Update update) {
        return !update.hasCallbackQuery() ? update.getMessage().getText() : "";
    }

    enum AWAITING_INPUT{
        add
    }
    //GETTER SETTER
}
