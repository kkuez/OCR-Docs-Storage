package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.google.common.annotations.VisibleForTesting;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.lang.model.util.Types;
import java.util.Map;
import java.util.Set;

public class MapQRItemProcess extends Process {
    int itemNumberToMap = 0;

    Step currentStep = null;
    private final static Set<String> commands = Set.of(
            "QR-Item mappen");

    public MapQRItemProcess(ProgressReporter reporter, BackendFacade facade, Update update, Bot bot) {
        super(reporter, facade);
        Message message = bot.sendMsg("Welches Item willst du mappen?", update, KeyboardFactory.KeyBoardType.QRItems, true, true);
        getSentMessages().add(message);
        currentStep = Step.chooseNumber;
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) throws TelegramApiException {
        Message message = null;
        switch (currentStep) {
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
                close(bot);
                break;
        }
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
        chooseNumber, nameItem
    }
}
