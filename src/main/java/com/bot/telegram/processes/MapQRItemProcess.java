package com.bot.telegram.processes;

import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.reporter.ProgressReporter;
import com.objectTemplates.User;

public class MapQRItemProcess extends Process {

    int itemNumberToMap = 0;

    Step currentStep = null;

    private final static Set<String> commands = Set.of("QR-Item mappen");

    public MapQRItemProcess(ProgressReporter reporter, BackendFacade facade) {
        super(reporter, facade);
        currentStep = Step.mapNewItem;
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) throws TelegramApiException {
        User user = bot.getNonBotUserFromUpdate(update);
        Message message = null;
        switch (currentStep) {
            case mapNewItem:
                message = bot.sendMsg("Welches Item willst du mappen?", update, KeyboardFactory.KeyBoardType.QRItems,
                        true, true);
                getSentMessages().add(message);
                currentStep = Step.chooseNumber;
                break;
            case chooseNumber:
                itemNumberToMap = Integer.parseInt(update.getCallbackQuery().getData());
                message = bot.simpleEditMessage("Und was..?", update, KeyboardFactory.KeyBoardType.Abort);
                bot.sendAnswerCallbackQuery("Und was?", false, update.getCallbackQuery());
                getSentMessages().add(message);
                currentStep = Step.nameItem;
                break;
            case nameItem:
                getFacade().updateQRItem(itemNumberToMap, update.getMessage().getText());
                message = bot.sendMsg("Ok :)", update, KeyboardFactory.KeyBoardType.NoButtons, false, false);
                getSentMessages().add(message);
                this.reset(bot, user);
                break;
        }
    }

    @Override
    public void reset(Bot bot, User user) {
        currentStep = Step.mapNewItem;
        super.reset(bot, user);
    }

    @Override
    public String getProcessName() {
        return null;
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        return null;
    }

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }

    private enum Step {
        chooseNumber, nameItem, mapNewItem
    }
}
