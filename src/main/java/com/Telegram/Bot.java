package com.Telegram;

import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.Utils.DBUtil;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bot extends TelegramLongPollingBot {

    public static Bot bot = null;

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        if(message != null && !message.equals("")){
            checkForCommands(update);
        }
        System.out.println(update.getMessage().getText());

       // sendMsg(update.getMessage().getChatId().toString(), message);
    }

    private void checkForCommands(Update update){
        String input = update.getMessage().getText().toLowerCase();
        List<Document> listOfDocs = new ArrayList<>();
        if(input.startsWith("search")){
             listOfDocs = DBUtil.getFilesForSearchTerm(input.substring(input.indexOf(" ")));
             System.out.println("Send list of Pictures related to \"" + input);
            sendMsg(update.getMessage().getChatId().toString(), "" + listOfDocs.size() + " Documents found :)");
        }else{
        if(input.startsWith("getpics")){

        }else{
        if(input.startsWith("")){

        }else{
        if(input.startsWith("")){

        }else{
        if(input.startsWith("")){

        }}}}}
    }
    /**
     * Method for creating a message and sending it.
     * @param chatId chat id
     * @param s The String that you want to send as a message.
     */
    public synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            execute(sendMessage);
            //sendMessage(sendMessage);
        } catch (TelegramApiException e) {
           e.printStackTrace();
        }
    }

    /**
     * This method returns the bot's name, which was specified during registration.
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return "NussBot";
    }

    /**
     * This method returns the bot's token for communicating with the Telegram server
     * @return the bot's token
     */
    @Override
    public String getBotToken() {
        return ObjectHub.getInstance().getProperties().getProperty("tgBotToken");
    }
public static void enterCommands(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Command 1 getupdate");
                while(true){
                    Scanner scanner = new Scanner(System.in);
                    switch (scanner.next()){
                        case "1":

                            break;
                    }
                }
            }
        });
}
}