package com.Telegram;

import com.Misc.Processes.*;
import com.Misc.Processes.Process;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.TessUtil;
import com.Utils.TimeUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    public Process process = null; //TODO Setter abfangen wenn neuer Prozess gestartet wird obwohl nicht null

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if(ObjectHub.getInstance().getAllowedUsersMap().keySet().contains(update.getMessage().getFrom().getId())){
            processUpdateReceveived(update);
        }else{
            if(process != null && process.getClass().equals(NewUserRegProcess.class)){
                process.performNextStep(update.getMessage().getText(), update);
            }else{
                BotUtil.sendMsg(update.getMessage().getChatId() + "", "Hallo " + update.getMessage().getFrom().getFirstName() + ", ich hab dich noch nicht im System gefunden, bitte gib das PW für NussBot ein:", this);
                process = new NewUserRegProcess(this);
            }
        }

    }
    private void processUpdateReceveived(Update update) {

        String message = update.getMessage().getText();

        if (message != null && !message.equals("")) {

            Process processToProcess = fetchCommandOrNull(update);
            if (process != null && processToProcess == null) {
                String input = update.getMessage().getText();
                if (input.startsWith("Japp")) {
                    process.performNextStep("Japp", update);
                } else {
                    if (input.startsWith("Nee")) {
                        process.performNextStep("Nee", update);
                    } else {
                        process.performNextStep(input, update);
                    }
                }
            }


            if (update.getMessage().hasPhoto()) {
                processPhoto(update);
            }
        }
    }

    private void processPhoto(Update update){
        File largestPhoto = null;
        List<PhotoSize> photoList = update.getMessage().getPhoto();
        photoList.sort(Comparator.comparing(PhotoSize::getFileSize));
        Collections.reverse(photoList);
        String filePath = getFilePath(photoList.get(0));
        largestPhoto = downloadPhotoByFilePath(filePath);
        File targetFile = new File(ObjectHub.getInstance().getArchiver().getDocumentFolder(), LocalDateTime.now().toString().replace(".", "-").replace(":", "_") + filePath.replace("/", ""));
        try {
            FileUtils.copyFile(largestPhoto, targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Boolean forceBon = update.getMessage().getCaption() != null && update.getMessage().getCaption().toLowerCase().contains("eatbon");

        Document document = TessUtil.processFile(targetFile, update.getMessage().getFrom().getId());
        try {

            if((TessUtil.checkIfBon(document.getContent()) || forceBon) && this != null){
                float sum = TessUtil.getLastNumber(document.getContent());
                Bon bon = new Bon(document.getContent(), targetFile, sum, document.getId());
                process = new BonProcess(bon, this, document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (process != null && process.getClass().equals(BonProcess.class)) {
            BotUtil.askBoolean("Das ist ein Bon oder?", update, Bot.this);
        }

        System.out.println(update.getMessage().getText());
        // sendMsg(update.getMessage().getChatId().toString(), message);

    }


    public void sendPhotoFromURL(Update update, String imagePath){
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

    private Process fetchCommandOrNull(Update update){
        String input = update.getMessage().getText();
        String cmd = input.contains(" ") ? input.substring(0, input.indexOf(" ")).toLowerCase().replace("/", "") : input.toLowerCase().replace("/", "");

        if(cmd.startsWith("search")){
            return new SearchProcess(this, update);
        }else{
            if(cmd.startsWith("getpics")){
                return new GetPicsProcess(this, update);
            }else{

                if(cmd.startsWith("getsum")){
                    return new SumProcess(this);
                }else{
                    if(cmd.startsWith("getbons")){
                        return new GetBonsProcess(this);
                    }else{
                        if(cmd.startsWith("removelast")) {
                            return new RemoveLastProcess(this);
                        }}}}}
        return null;
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