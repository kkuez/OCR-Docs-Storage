package com.Telegram;

import com.Misc.Processes.BonProcess;
import com.Misc.Processes.NewUserRegProcess;
import com.Misc.Processes.Process;
import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.TessUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.print.Doc;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    public static Bot bot = null;

    public static Process process = null;

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
       if(ObjectHub.getInstance().getAllowedUsersMap().keySet().contains(update.getMessage().getFrom().getId())){
           processUpdateReceveived(update);
       }else{
           if(Bot.process != null && Bot.process.getClass().equals(NewUserRegProcess.class)){
               Bot.process.performNextStep(update.getMessage().getText(), update);
           }else{
               BotUtil.sendMsg(update.getMessage().getChatId() + "", "Hallo " + update.getMessage().getFrom().getFirstName() + ", ich hab dich noch nicht im System gefunden, bitte gib das PW für NussBot ein:", Bot.bot);
                Bot.process = new NewUserRegProcess();
           }
       }

    }
    private void processUpdateReceveived(Update update){
        String message = update.getMessage().getText();
        if (message != null && !message.equals("")) {
            checkForCommands(update);
        }

        if (update.getMessage().hasPhoto()) {
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

            TessUtil.processFile(targetFile, update.getMessage().getFrom().getId(), bot);
            if (process != null && process.getClass().equals(BonProcess.class)) {
                BotUtil.askBoolean("Das ist ein Bon oder?", update, Bot.this);
            }

            System.out.println(update.getMessage().getText());
            // sendMsg(update.getMessage().getChatId().toString(), message);

        }
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
        List<Document> listOfDocs = new ArrayList<>();
        if(input.toLowerCase().startsWith("search")){
             listOfDocs = DBUtil.getFilesForSearchTerm(searchTerm);
             System.out.println("Send list of Pictures related to \"" + input);
            BotUtil.sendMsg(update.getMessage().getChatId().toString(), "" + listOfDocs.size() + " Documents found :)", Bot.this);
        }else{
        if(input.toLowerCase().startsWith("getpics")){
            listOfDocs = DBUtil.getFilesForSearchTerm(searchTerm);
            listOfDocs.forEach(document -> sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath()));
        }else{
        if(input.startsWith("Japp")){
            process.performNextStep("Japp", update);
        }else{
        if(input.startsWith("Nee")){
            process.performNextStep("Nee", update);

        }else{
            process.performNextStep(input, update);
        }}}}
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