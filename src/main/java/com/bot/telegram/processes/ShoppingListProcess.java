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
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.*;

public class ShoppingListProcess extends Process{

    private AWAITING_INPUT status = null;

    User user;

    public ShoppingListProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap, BackendFacade facade){
        super(progressReporter, facade);
        this.setBot(bot);
        user = getBot().getNonBotUserFromUpdate(update);
        user.setBusy(true);
        performNextStep("-", update,  allowedUsersMap);
    }

    private void sendShoppingList(Update update){
        StringBuilder listeBuilder = new StringBuilder("*Aktuelle Einkaufsliste:*\n");
        for(int i = 0;i<getBot().getShoppingList().size();i++){
            listeBuilder.append( i + ": " + getBot().getShoppingList().get(i) + "\n");
        }
        getBot().sendMsg(listeBuilder.toString(), update, null, false, false, Bot.ParseMode.Markdown);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        String[] commandValue = deserializeInput(update);
        Message message = null;
        switch (commandValue[0]){
            case "add":
                String item = commandValue[1];
                getBot().getShoppingList().add(item);
                getFacade().insertShoppingItem(item);
                if(update.hasCallbackQuery()){
                    try {
                        getBot().sendAnswerCallbackQuery(item + " hinzugefügt! :) Noch was?", false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        logger.error("Failed activating bot", e);
                    }
                }else{
                    message = getBot().sendMsg(item + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done, false, true);
                }
                getSentMessages().add(message);
                break;
            case "remove":
                try{
                    item = commandValue[1];
                    getFacade().deleteFromShoppingList(item);
                    getBot().getShoppingList().remove(item);
                    getBot().sendAnswerCallbackQuery(item + " gelöscht. Nochwas?", false, update.getCallbackQuery());
                    getBot().simpleEditMessage(item + " gelöscht. Nochwas?", getBot().getMassageFromUpdate(update), KeyboardFactory.KeyBoardType.ShoppingList_Current, "remove");
                }catch (Exception e){
                    logger.error(null, e);
                }
                break;
            case "done":
                getBot().sendMsg("Ok :)", update, null, false, false);
                close();
                break;
            case "Einkaufsliste anzeigen":
                sendShoppingList(update);
                close();
                break;
            case "Liste Löschen":
                getBot().getShoppingList().forEach(shoppingItem -> getFacade().deleteFromShoppingList(shoppingItem));
                getBot().setShoppingList(new ArrayList<>());
                getBot().sendMsg("Einkaufsliste gelöscht :)", update, null, false, false);
                close();
                break;
            case "Löschen":
                ReplyKeyboard shoppingListKeyboard = KeyboardFactory.getInlineKeyboardForList(getFacade().getShoppingList(), "remove");
                message = getBot().sendKeyboard("Was soll gelöscht werden?", update, shoppingListKeyboard, false);
                break;
            case "Zu Einkaufsliste hinzufügen":
            case "Hinzufügen":
                message = getBot().sendMsg("Was soll hinzugefügt werden?", update, KeyboardFactory.KeyBoardType.ShoppingList_Add, false, true);
                status = AWAITING_INPUT.add;
                break;
            case"Standardliste anzeigen":
                try {
                    message = getBot().simpleEditMessage("Standardliste:", update, KeyboardFactory.KeyBoardType.StandardList_Current, "add");
                } catch (TelegramApiException e) {
                    if(((TelegramApiRequestException) e).getApiResponse().contains("message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message")){
                        logger.info("Message not edited, no need.");
                    }else{
                        logger.error(((TelegramApiRequestException) e).getApiResponse(), e);
                    }
                }
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
