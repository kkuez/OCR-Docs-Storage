package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.Set;

public class StartProcess extends Process {
    private static Set<String> commands = Set.of(
            "start");

    public StartProcess(Bot bot, Update update, ProgressReporter progressReporter, BackendFacade facade){
        super(progressReporter, facade);
        bot.sendMsg("Wähle eine Aktion:", update, KeyboardFactory.KeyBoardType.Start, true, false);
        close(bot);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {

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
