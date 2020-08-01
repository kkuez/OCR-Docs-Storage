package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.bot.telegram.Bot;
import com.objectTemplates.User;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

public class RemoveLastProcess extends Process {

    private Set<String> commands = Set.of("Letztes Bild Löschen");

    public RemoveLastProcess(ProgressReporter progressReporter, BackendFacade facade){
        super(progressReporter, facade);
    }

    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        User user = bot.getNonBotUserFromUpdate(update);
        getFacade().deleteLastDocument();
        bot.sendMsg( "Letztes Bild gelöscht :)", update, null, true, false);
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
