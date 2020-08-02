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

public class ShoppingListProcess extends Process {

    private AWAITING_INPUT status = null;

    private final static Set<String> commands = Set.of(
            "Einkaufsliste anzeigen",
            "Liste Löschen",
            "Löschen",
            "Zu Einkaufsliste hinzufügen",
            "Hinzufügen",
            "Einkaufslisten-Optionen",
            "Standardliste anzeigen");

    public ShoppingListProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
    }

    private void sendShoppingList(Update update, Bot bot) {
        StringBuilder listeBuilder = new StringBuilder("*Aktuelle Einkaufsliste:*\n");
        for (int i = 0; i < bot.getShoppingList().size(); i++) {
            listeBuilder.append(i + ": " + bot.getShoppingList().get(i) + "\n");
        }
        bot.sendMsg(listeBuilder.toString(), update, null, false, false, Bot.ParseMode.Markdown);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        User user = bot.getNonBotUserFromUpdate(update);
        String[] commandValue = deserializeInput(update, bot);
        Message message = null;
        switch (commandValue[0]) {
            case "add":
                String item = commandValue[1];
                bot.getShoppingList().add(item);
                getFacade().insertShoppingItem(item);
                if (update.hasCallbackQuery()) {
                    try {
                        bot.sendAnswerCallbackQuery(item + " hinzugefügt! :) Noch was?", false, update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        logger.error("Failed activating bot", e);
                    }
                } else {
                    message = bot.sendMsg(item + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done, false, true);
                }
                getSentMessages().add(message);
                break;
            case "remove":
                try {
                    item = commandValue[1];
                    getFacade().deleteFromShoppingList(item);
                    bot.getShoppingList().remove(item);
                    bot.sendAnswerCallbackQuery(item + " gelöscht. Nochwas?", false, update.getCallbackQuery());
                    bot.simpleEditMessage(item + " gelöscht. Nochwas?", bot.getMassageFromUpdate(update), KeyboardFactory.KeyBoardType.ShoppingList_Current, "remove");
                } catch (Exception e) {
                    logger.error(null, e);
                }
                break;
            case "done":
                bot.sendMsg("Ok :)", update, null, false, false);
                reset(bot, user);
                break;
            case "Einkaufsliste anzeigen":
                sendShoppingList(update, bot);
                reset(bot, user);
                break;
            case "Liste Löschen":
                bot.getShoppingList().forEach(shoppingItem -> getFacade().deleteFromShoppingList(shoppingItem));
                bot.setShoppingList(new ArrayList<>());
                bot.sendMsg("Einkaufsliste gelöscht :)", update, null, false, false);
                reset(bot, user);
                break;
            case "Löschen":
                ReplyKeyboard shoppingListKeyboard = KeyboardFactory.getInlineKeyboardForList(getFacade().getShoppingList(), "remove");
                message = bot.sendKeyboard("Was soll gelöscht werden?", update, shoppingListKeyboard, false);
                break;
            case "Zu Einkaufsliste hinzufügen":
            case "Hinzufügen":
                message = bot.sendMsg("Was soll hinzugefügt werden?", update, KeyboardFactory.KeyBoardType.ShoppingList_Add, false, true);
                status = AWAITING_INPUT.add;
                break;
            case "Einkaufslisten-Optionen":
                bot.sendKeyboard("Was willst du tun?", update, KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.ShoppingList, false, false, null, getFacade()), false);
                break;
            case "Standardliste anzeigen":
                try {
                    message = bot.simpleEditMessage("Standardliste:", update, KeyboardFactory.KeyBoardType.StandardList_Current, "add");
                } catch (TelegramApiException e) {
                    if (((TelegramApiRequestException) e).getApiResponse().contains("message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message")) {
                        logger.info("Message not edited, no need.");
                    } else {
                        logger.error(((TelegramApiRequestException) e).getApiResponse(), e);
                    }
                }
                break;
            default:
                reset(bot, user);
                user = bot.getAllowedUsersMap().get(update.getMessage().getChatId());
        }
        user.setBusy(false);
        if (message != null) {
            getSentMessages().add(message);
        }
    }

    @Override
    public String getProcessName() {
        return "Shoppinglist Process";
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("Standardliste anzeigen")) {
            return "Standardliste anzeigen";
        } else {
            if (status == AWAITING_INPUT.add) {
                return "add";
            } else {
                if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("remove")) {
                    return "remove";
                }
            }
        }

        return !update.hasCallbackQuery() ? update.getMessage().getText() : "";
    }

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }

    enum AWAITING_INPUT {
        add
    }

    //GETTER SETTER

}
