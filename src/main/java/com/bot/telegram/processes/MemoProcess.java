package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

public class MemoProcess extends Process {

    Map<Integer, User> allowedUsersMap;

    User user;

    InputType inputType = null;


    public MemoProcess(ProgressReporter reporter, Bot bot, Update update, Map<Integer, User> allowedUsersMap, BackendFacade facade) {
        super(reporter, facade);
        this.setBot(bot);
        this.allowedUsersMap = allowedUsersMap;
        user = getBot().getNonBotUserFromUpdate(update);
        try {
            performNextStep("", update, allowedUsersMap);
        } catch (TelegramApiException e) {
            logger.error("Failed activating bot", e);
        }
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) throws TelegramApiException {
        Message message = null;
        String[] commandValue = deserializeInput(update);
        switch (commandValue[0]){
            case "Memos anzeigen":
                sendMemoList(update);
                close();
                break;
            case "Memos löschen":
                inputType = InputType.remove;
                ReplyKeyboard memoListKeyboard = KeyboardFactory.getInlineKeyboardForList(getFacade().getMemos(user.getId()), "remove");
                message = getBot().sendKeyboard("Was soll gelöscht werden?", update, memoListKeyboard, false);
                break;
            case "Memo hinzufügen":
                inputType = InputType.add;
                message = getBot().sendMsg("Was soll hinzugefügt werden?", update, null, true, false);
                break;
            case "add":
                String item = commandValue[1];
                getFacade().insertMemo(item, user.getId());
                message = getBot().sendMsg(item + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done, false, true);
                getSentMessages().add(message);
                break;
            case "remove":
                try{
                    item = commandValue[1];
                    getFacade().deleteMemo(item);
                    getBot().sendAnswerCallbackQuery(item + " gelöscht. Nochwas?", false, update.getCallbackQuery());
                    getBot().simpleEditMessage(item + " gelöscht. Nochwas?", getBot().getMassageFromUpdate(update), KeyboardFactory.getInlineKeyboardForList(getFacade().getMemos(user.getId()), "remove"), "remove");
                }catch (Exception e){
                    logger.error(null, e);
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
            }else{
                if(inputType == InputType.remove){
                    return "remove";
                }
            }
            }
        return null;
    }

    private void sendMemoList(Update update){
        List<String> memoList = getFacade().getMemos(user.getId());
        StringBuilder listeBuilder = new StringBuilder("*Aktuelle Memos:*\n");
        for(int i = 0;i<memoList.size();i++){
            listeBuilder.append( i + ": " + memoList.get(i) + "\n");
        }
        getBot().sendMsg(listeBuilder.toString(), update, null, false, false, Bot.ParseMode.Markdown);
    }

    enum InputType{
        add, remove
    }
}
