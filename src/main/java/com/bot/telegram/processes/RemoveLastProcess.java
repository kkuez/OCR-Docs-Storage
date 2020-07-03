package com.bot.telegram.processes;

import com.backend.BackendFacade;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class RemoveLastProcess extends Process {

    public RemoveLastProcess(Bot bot, ProgressReporter progressReporter, Update update, BackendFacade facade){
        super(progressReporter, facade);
    performNextStep("asd", update, bot);
    }
    @Override
    public void performNextStep(String arg, Update update, Bot bot) {
        getFacade().deleteLastDocument();
        bot.sendMsg( "Letztes Bild gelöscht :)", update, null, true, false);
        close(bot);
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
        //TODO
        return false;
    }
}
