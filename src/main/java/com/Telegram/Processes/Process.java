package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import com.Utils.BotUtil;
import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Process {

    public Process(ProgressReporter reporter){
        progressReporter = reporter;
    }

    private ProgressReporter progressReporter;

    private Bot bot;

    public Document document;

    private Boolean hasStarted = false;

    private boolean deleteLater = false;

    public abstract void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap);

    public abstract String getProcessName();

    public List<Message> sentMessages = new ArrayList<>();

    public void clearButtons(){
        for(Message message : getSentMessages()){
            if(message != null){
            BotUtil.simpleEditMessage(message.getText(), getBot(), message, KeyboardFactory.KeyBoardType.NoButtons);
        }}
}

    public void close(){
        clearButtons();
        setDeleteLater(true);
    }

    //GETTER SETTER


    public synchronized List<Message> getSentMessages() {
        return sentMessages;
    }

    public boolean isDeleteLater() {
        return deleteLater;
    }

    public boolean getDeleteLater() {
        return deleteLater;
    }

    public void setDeleteLater(boolean deleteLater) {
        this.deleteLater = deleteLater;
    }

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
