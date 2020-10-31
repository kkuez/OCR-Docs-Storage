package com.bot.telegram.processes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.reporter.ProgressReporter;
import com.objectTemplates.User;

public class StandardListProcess extends Process {

    AWAITING_INPUT status = null;

    private final static Set<String> commands = Set.of("Standardliste anzeigen", "Liste Löschen", "Löschen",
            "Hinzufügen");

    public StandardListProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        User user = bot.getNonBotUserFromUpdate(update);
        String[] commandValue = deserializeInput(update, bot);
        Message message = null;
        switch (commandValue[0]) {
            case "add":
                String item = commandValue[1];
                getFacade().insertToStandartList(item);
                message = bot.sendMsg(item + " hinzugefügt! :) Noch was?", update, KeyboardFactory.KeyBoardType.Done,
                        false, true);
                getSentMessages().add(message);
                break;
            case "remove":
                try {
                    item = commandValue[1];
                    getFacade().deleteFromStandartList(item);
                    bot.sendAnswerCallbackQuery(item + " gelöscht. Nochwas?", false, update.getCallbackQuery());
                    bot.simpleEditMessage(item + " gelöscht. Nochwas?", bot.getMassageFromUpdate(update),
                            KeyboardFactory.KeyBoardType.StandardList_Current, "remove");
                } catch (Exception e) {
                    logger.error(null, e);
                }
                break;
            case "done":
                bot.sendMsg("Ok :)", update, null, false, false);
                reset(bot, user);
                break;
            case "Standardliste anzeigen":
                // TODO Man kann nicht zweimal hintereinander Standartliste anzeigen ZB denn der Process wird
                // geschlossen. Wenn der Prozess null ist dann wird gewartet bis gültige eingabe kommt
                sendStandardList(update, bot);
                reset(bot, user);
                break;
            case "Liste Löschen":
                List<String> standartList = getFacade().getStandartList();
                standartList.forEach(standartListItem -> getFacade().deleteFromStandartList(standartListItem));
                bot.setShoppingList(new ArrayList<String>());
                bot.sendMsg("Standardliste gelöscht :)", update, null, false, false);
                reset(bot, user);
                break;
            case "Löschen":
                ReplyKeyboard standardListKeyboard = KeyboardFactory
                        .getInlineKeyboardForList(getFacade().getStandartList(), "remove");
                message = bot.sendKeyboard("Was soll gelöscht werden?", update, standardListKeyboard, false);
                break;
            case "Hinzufügen":
                message = bot.sendMsg("Was soll hinzugefügt werden?", update, KeyboardFactory.KeyBoardType.Abort, false,
                        true);
                status = AWAITING_INPUT.add;
                break;
            default:
                bot.getAllowedUsersMap().get(update.getMessage().getChatId()).setBusy(true);
        }
        bot.getNonBotUserFromUpdate(update).setBusy(false);
        if (message != null) {
            getSentMessages().add(message);
        }
    }

    private void sendStandardList(Update update, Bot bot) {
        StringBuilder listeBuilder = new StringBuilder("Aktuelle Standardliste:\n");
        List<String> standardList = getFacade().getStandartList();
        for (int i = 0; i < standardList.size(); i++) {
            listeBuilder.append(i + ": " + standardList.get(i) + "\n");
        }
        bot.sendMsg(listeBuilder.toString(), update, null, false, false);
    }

    @Override
    public String getProcessName() {
        return null;
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        String prefix = !update.hasCallbackQuery() ? update.getMessage().getText() : "";
        prefix = status == AWAITING_INPUT.add ? "add" : prefix;
        return prefix;
    }

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }

    enum AWAITING_INPUT {
        add
    }
    // GETTER SETTER
}
