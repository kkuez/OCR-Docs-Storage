package com.Telegram;

import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.Utils.DBUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

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

        if(update.getMessage().getPhoto().size() != 0){
            File largestPhoto = null;
            List<PhotoSize> photoList = update.getMessage().getPhoto();
            photoList.sort(Comparator.comparing(PhotoSize::getFileSize));
            Collections.reverse(photoList);
            String filePath = getFilePath(photoList.get(0));
            largestPhoto = downloadPhotoByFilePath(filePath);
            try {
                FileUtils.copyFile(largestPhoto, new File("/home/marcel/Muell", LocalDateTime.now().toString().replace(".", "-")));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        System.out.println(update.getMessage().getText());
       // sendMsg(update.getMessage().getChatId().toString(), message);

    }

    private void sendPhotoFromURL(Update update, String imagePath){
        SendPhoto sendPhoto = null;
        try {
            sendPhoto = new SendPhoto().setPhoto("SomeText", new FileInputStream(new File(imagePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        sendPhoto.setChatId(update.getMessage().getChatId());
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public java.io.File downloadPhotoByFilePath(String filePath) {
        try {
            // Download the file calling AbsSender::downloadFile method
            return downloadFile(filePath);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getFilePath(PhotoSize photo) {
        Objects.requireNonNull(photo);

        if (photo.hasFilePath()) { // If the file_path is already present, we are done!
            return photo.getFilePath();
        } else { // If not, let find it
            // We create a GetFile method and set the file_id from the photo
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(photo.getFileId());
            try {
                // We execute the method using AbsSender::execute method.
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);
                // We now have the file_path
                return file.getFilePath();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        return null; // Just in case
    }

    private void checkForCommands(Update update){
        String input = update.getMessage().getText();
        String searchTerm = input.substring(input.indexOf(" ") + 1);
        String cmd = input.substring(0, input.indexOf(" ")).toLowerCase();
        List<Document> listOfDocs = new ArrayList<>();
        if(cmd.startsWith("search")){
             listOfDocs = DBUtil.getFilesForSearchTerm(searchTerm);
             System.out.println("Send list of Pictures related to \"" + input);
            sendMsg(update.getMessage().getChatId().toString(), "" + listOfDocs.size() + " Documents found :)");
        }else{
        if(cmd.startsWith("getpics")){
            listOfDocs = DBUtil.getFilesForSearchTerm(searchTerm);
            listOfDocs.forEach(document -> sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath()));
        }else{
        if(cmd.startsWith("bon")){

        }else{
        if(cmd.startsWith("")){

        }else{
        if(cmd.startsWith("")){

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