package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.KeyboardFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

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

    private boolean awaitsInput = false;

    public abstract void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap);

    public abstract String getProcessName();

    public List<Message> sentMessages = new ArrayList<>();


    public void clearButtons(){
        for(Message message : getSentMessages()){
            if(message != null){
            getBot().simpleEditMessage(message.getText(), message, KeyboardFactory.KeyBoardType.NoButtons, "");
        }}
}

    public void close(){
        clearButtons();
        setDeleteLater(true);
    }

    public String[] deserializeInput(Update update){
        String command = getCommandIfPossible(update);
        String updateText = update.hasCallbackQuery() ? update.getCallbackQuery().getData() :  getBot().getMassageFromUpdate(update).getText();
        String value = updateText.replace(command, "");

        if(update.hasCallbackQuery()){
             updateText = update.getCallbackQuery().getData();
            if(updateText.startsWith("remove")){
                command = "remove";
                value = updateText.replace(command, "");
            }else{
                if(updateText.startsWith("add")){
                    command = "add";
                    value = updateText.replace(command, "");
                }else{
                    if(updateText.startsWith("done")){
                        command = "done";
                    }
                }
            }
        }
        return new String[]{command, value};
    }

    public abstract String getCommandIfPossible(Update update);

    //GETTER SETTER


    public boolean isAwaitsInput() {
        return awaitsInput;
    }

    public void setAwaitsInput(boolean awaitsInput) {
        this.awaitsInput = awaitsInput;
    }

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
