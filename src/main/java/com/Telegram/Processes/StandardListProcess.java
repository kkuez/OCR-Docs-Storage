package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.Item;
import com.Telegram.KeyboardFactory;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StandardListProcess extends Process {

    private File picturesFolder;

    private List<Item> standardList;

    private String action = null;

    private Item item = null;

    public StandardListProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        standardList = DBUtil.getStandardListFromDB();
        checkForPictureFolder();
        performNextStep("asd", update, allowedUsersMap);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        //Terms in this set need more userinformation in a further step
        Set<String> commandsWithLaterExecution = Set.of("Item hinzufügen", "Item löschen");
        if(action != null){
            item = DBUtil.getStandardItem(getBot().getMassageFromUpdate(update).getText());
            if(action.equals("removeitem")){
                try{
                    item = DBUtil.getStandardItem(update.getCallbackQuery().getData());
                    DBUtil.executeSQL("delete from StandardList where item='" +  item.getName() + "'");
                    standardList.remove(item);
                    getBot().sendAnswerCallbackQuery(item.getName() + " gelöscht.", false, update.getCallbackQuery());
                    getBot().simpleEditMessage(item.getName() + " gelöscht. Nochwas?", getBot().getMassageFromUpdate(update), KeyboardFactory.KeyBoardType.StandardList_Current);
                }catch (Exception e){
                    LogUtil.logError(null, e);
                }
            }
        }
        if(!commandsWithLaterExecution.contains(getBot().getMassageFromUpdate(update).getText())){
            processInOneStep(arg, update, allowedUsersMap);
        }else{
            prepareForProcessing(update);
        }
        getBot().setBusy(false);
    }

    private void prepareForProcessing(Update update) {
        Message message = null;
        switch (update.getMessage().getText()){
            case "Item hinzufügen":
                message = getBot().sendMsg("Was soll hinzugefügt werden?", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                action = "add";
                break;
            case "Item löschen":
                List<String> standardItemNames = new ArrayList<>();
                DBUtil.getStandardListFromDB().forEach(item1 -> standardItemNames.add(item1.getName()));
                ReplyKeyboard shoppingListKeyboard = KeyboardFactory.getInlineKeyboardForList(standardItemNames);
                message = getBot().sendKeyboard("Was soll gelöscht werden?", update, shoppingListKeyboard, false);
                action = "removeitem";
                break;
        }
        getSentMessages().add(message);
    }

    private void processInOneStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        String input = null;
        String cmd = arg;
        if(arg.equals("done")){
            input = "done";
        }else{
            if(item != null){
                input = action + " " + item.getName();
            }else{
                input = getBot().getMassageFromUpdate(update).getText();
            }}

        switch (input){
            case "done":
                getBot().sendMsg("Ok :)", update, null, false, false);
                close();
                break;
            case "Standardliste anzeigen":
                StringBuilder stringBuilder = new StringBuilder();
                DBUtil.getStandardListFromDB().forEach(item1 -> stringBuilder.append(item1.getName()));
                getBot().sendMsg(stringBuilder.toString(), update, KeyboardFactory.KeyBoardType.Abort, false, true);
                close();
                break;
            default:
                if((input.contains("add") || input.contains("removeitem"))){
                    cmd = input.substring(0, input.indexOf(" ")).toLowerCase();
                }
                break;
        }

        arg = input.substring(input.indexOf(" ") + 1);
        switch (cmd){
            case "add":
                standardList.add(arg);
                DBUtil.executeSQL("insert into StandardList(item) Values ('" + arg + "')");
                Message message = getBot().sendMsg(arg + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done, false, true);
                getSentMessages().add(message);
                break;
        }
    }

    @Override
    public String getProcessName() {
        return "StandardList";
    }

    private void checkForPictureFolder(){
        picturesFolder = new File(ObjectHub.getInstance().getArchiver().getResourceFolder(), "StandardListPictures");
        if(!picturesFolder.exists()){
            picturesFolder.mkdir();
        }
    }
}
