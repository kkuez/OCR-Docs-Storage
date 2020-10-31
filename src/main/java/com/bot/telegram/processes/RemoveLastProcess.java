package com.bot.telegram.processes;

import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.backend.BackendFacade;
import com.bot.telegram.Bot;
import com.reporter.ProgressReporter;
import com.objectTemplates.User;

public class RemoveLastProcess extends Process {

    private Set<String> commands = Set.of("Letztes Bild Löschen");

    public RemoveLastProcess(ProgressReporter progressReporter, BackendFacade facade) {
        super(progressReporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        User user = bot.getNonBotUserFromUpdate(update);
        getFacade().deleteLastDocument();
        bot.sendMsg("Letztes Bild gelöscht :)", update, null, true, false);
        reset(bot, user);
    }

    @Override
    public String getProcessName() {
        return "Remove last Picture";
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
