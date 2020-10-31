package com.bot.telegram.processes;

import java.util.List;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.reporter.ProgressReporter;
import com.objectTemplates.User;

public class MemoProcess extends Process {

    InputType type = null;

    private User user = null;

    private final static Set<String> commands = Set.of("Memos anzeigen", "Memos löschen", "Memo hinzufügen",
            "Memo-Optionen", "add", "remove");

    public MemoProcess(ProgressReporter reporter, BackendFacade facade) {
        super(reporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) throws TelegramApiException {
        User user = bot.getNonBotUserFromUpdate(update);
        if (user == null) {
            user = bot.getNonBotUserFromUpdate(update);
        }
        Message message = null;
        String[] commandValue = deserializeInput(update, bot);
        switch (commandValue[0]) {
            case "Memo-Optionen":
                bot.sendKeyboard("Was willst du tun?", update,
                        KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Memo, false, false, null, getFacade()),
                        false);
                reset(bot, user);
                break;
            case "Memos anzeigen":
                sendMemoList(update, bot);
                reset(bot, user);
                break;
            case "Memos löschen":
                type = InputType.remove;
                ReplyKeyboard memoListKeyboard = KeyboardFactory
                        .getInlineKeyboardForList(getFacade().getMemos(user.getId()), "remove");
                message = bot.sendKeyboard("Was soll gelöscht werden?", update, memoListKeyboard, false);
                break;
            case "Memo hinzufügen":
                type = InputType.add;
                message = bot.sendMsg("Was soll hinzugefügt werden?", update, null, true, false);
                break;
            case "done":
                bot.sendMsg("Ok :)", update, null, false, false);
                reset(bot, user);
                break;
            default:
                switch (type) {
                    case add:
                        String item = commandValue[1];
                        getFacade().insertMemo(item, user.getId());
                        message = bot.sendMsg(item + " hinzugefügt! :) Noch was?", update,
                                KeyboardFactory.KeyBoardType.Done, false, true);
                        getSentMessages().add(message);
                        break;
                    case remove:
                        try {
                            item = commandValue[1];
                            getFacade().deleteMemo(item);
                            bot.sendAnswerCallbackQuery(item + " gelöscht. Nochwas?", false, update.getCallbackQuery());
                            bot.simpleEditMessage(
                                    item + " gelöscht. Nochwas?", bot.getMassageFromUpdate(update), KeyboardFactory
                                            .getInlineKeyboardForList(getFacade().getMemos(user.getId()), "remove"),
                                    "remove");
                        } catch (Exception e) {
                            logger.error(null, e);
                        }
                        break;
                }
                break;
        }

        user.setBusy(false);
        if (message != null) {
            getSentMessages().add(message);
        }
    }

    @Override
    public String getProcessName() {
        return "MemoProcess";
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        String inputString = update.hasCallbackQuery() ? update.getCallbackQuery().getData()
                : update.getMessage().getText();
        if (inputString.startsWith("Memo")) {
            return inputString;
        } else {
            if (type == InputType.add) {
                return "add";
            } else {
                if (type == InputType.remove) {
                    return "remove";
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }

    private void sendMemoList(Update update, Bot bot) {
        User user = bot.getNonBotUserFromUpdate(update);
        List<String> memoList = getFacade().getMemos(user.getId());
        StringBuilder listeBuilder = new StringBuilder("*Aktuelle Memos:*\n");
        for (int i = 0; i < memoList.size(); i++) {
            listeBuilder.append(i + ": " + memoList.get(i) + "\n");
        }
        bot.sendMsg(listeBuilder.toString(), update, null, false, false, Bot.ParseMode.Markdown);
    }

    enum InputType {
        add, remove
    }
}
