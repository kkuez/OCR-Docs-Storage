package com.bot.telegram.processes;

import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;

public class StartProcess extends Process {

    private static Set<String> commands = Set.of("Start");

    public StartProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        User user = bot.getNonBotUserFromUpdate(update);
        bot.sendMsg("Wähle eine Aktion:", update, KeyboardFactory.KeyBoardType.Start, true, false);
        reset(bot, user);
    }

    @Override
    public String getProcessName() {
        return "Start";
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
