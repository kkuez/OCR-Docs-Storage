package com.bot.telegram.processes;

import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;

public class FurtherOptionsProcess extends Process {

    private static Set<String> commands = Set.of("Weitere Optionen");

    public FurtherOptionsProcess(ProgressReporter reporter, BackendFacade facade) {
        super(reporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) throws TelegramApiException {
        User user = bot.getNonBotUserFromUpdate(update);
        bot.sendMsg("Wähle eine Aktion:", update, KeyboardFactory.KeyBoardType.FurtherOptions, true, false);
        reset(bot, user);
    }

    @Override
    public String getProcessName() {
        return "";
    }

    @Override
    public String getCommandIfPossible(Update update, Bot bot) {
        return null;
    }

    @Override
    public boolean hasCommand(String cmd) {
        return commands.contains(cmd);
    }
}
