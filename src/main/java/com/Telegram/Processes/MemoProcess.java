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

import java.util.Map;

public class MemoProcess extends Process {

    Map<Integer, User> allowedUsersMap;

    User user;

    InputType inputType = null;


    public MemoProcess(ProgressReporter reporter, Bot bot, Update update, Map<Integer, User> allowedUsersMap) {
        super(reporter);
        this.setBot(bot);
        this.allowedUsersMap = allowedUsersMap;
        user = getBot().getNonBotUserFromUpdate(update);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) throws TelegramApiException {
        Message message = null;
        String[] commandValue = deserializeInput(update);
        switch (commandValue[0]){
            case "Memos anzeigen":

                break;
            case "Memos löschen":
                inputType = InputType.remove;
                ReplyKeyboard memoListKeyboard = KeyboardFactory.getInlineKeyboardForList(DBUtil.getMemoListFromDB(), "remove");
                message = getBot().sendKeyboard("Was soll gelöscht werden?", update, memoListKeyboard, false);
                break;
            case "Memo hinzufügen":
                inputType = InputType.add;
                message = getBot().sendMsg("Was soll hinzugefügt werden?", update, null, true, false);
                break;
            case "add":
                String item = commandValue[1];
                DBUtil.executeSQL("insert into Memos(item) Values ('" + item + "')");
                message = getBot().sendMsg(item + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done, false, true);
                getSentMessages().add(message);
                break;
            case "remove":
                try{
                    item = commandValue[1];
                    DBUtil.executeSQL("delete from Memos where item='" +  item + "'");
                    getBot().sendAnswerCallbackQuery(item + " gelöscht. Nochwas?", false, update.getCallbackQuery());
                    getBot().simpleEditMessage(item + " gelöscht. Nochwas?", getBot().getMassageFromUpdate(update), KeyboardFactory.getInlineKeyboardForList(DBUtil.getMemoListFromDB(), "remove"), "remove");
                }catch (Exception e){
                    LogUtil.logError(null, e);
                }
                break;
            case "done":
                getBot().sendMsg("Ok :)", update, null, false, false);
                close();
                break;
        }


        user.setBusy(false);
        if(message != null){
            getSentMessages().add(message);
        }
    }

    @Override
    public String getProcessName() {
        return "MemoProcess";
    }

    @Override
    public String getCommandIfPossible(Update update) {
        String inputString = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : update.getMessage().getText();
        if (inputString.startsWith("Memo")) {
            return inputString;
        } else {
            if(inputType == InputType.add){
                return "add";
            }
            }
        return null;
    }

    enum InputType{
        add, remove
    }
}
