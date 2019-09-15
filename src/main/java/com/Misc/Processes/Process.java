package com.Misc.Processes;

import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class Process {

    private Bot bot;

    public Document document;

    private Boolean hasStarted = false;

    public abstract void performNextStep(String arg, Update update);

    //GETTER SETTER
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
