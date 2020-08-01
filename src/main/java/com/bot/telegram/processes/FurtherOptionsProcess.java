package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Set;

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
}
