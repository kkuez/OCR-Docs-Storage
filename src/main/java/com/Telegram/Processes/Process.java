package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public abstract class Process {

    public Process(ProgressReporter reporter){
        progressReporter = reporter;
    }

    private ProgressReporter progressReporter;

    private Bot bot;

    public Document document;

    private Boolean hasStarted = false;

    public abstract void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap);

    public abstract String getProcessName();

    //GETTER SETTER


    public ProgressReporter getProgressReporter() {
        return progressReporter;
    }

    public void setProgressReporter(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }


    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Boolean getHasStarted() {
        return hasStarted;
    }

    public void setHasStarted(Boolean hasStarted) {
        this.hasStarted = hasStarted;
    }
}
