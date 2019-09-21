package com.Telegram;

import com.Telegram.Processes.*;
import com.Telegram.Processes.Process;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.Utils.BotUtil;
import com.Utils.DBUtil;
import com.Utils.TessUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    public Process process = null; //TODO Setter abfangen wenn neuer Prozess gestartet wird obwohl nicht null

    private Boolean isBusy;

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        printUpdateData(update);

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

            if(update.getMessage().getText() != null) {
                String input = update.getMessage().getText();
                if (process == null) {
                    process = fetchCommandOrNull(update);
                } else {
                    if (getBusy()) {
                        BotUtil.sendMsg(update.getMessage().getChatId() + "", "Bin am arbeiten...", process.getBot());
                    } else {
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
                }
            }
            if (update.getMessage().hasPhoto()) {
                processPhoto(update);
            }
        }

        private void printUpdateData(Update update){
            StringBuilder printBuilder = new StringBuilder(LocalDateTime.now().toString() + ":    Update from " + update.getMessage().getFrom().getFirstName());
            String append = update.getMessage().hasPhoto() ? ", picture: " +update.getMessage().getPhoto().toString() : "";
            printBuilder.append(append);
            append = update.getMessage().hasText() ? ", cmd: " +update.getMessage().getText() : "";
            printBuilder.append(append);
            System.out.println(printBuilder);
        }

    private void processPhoto(Update update){
        setBusy(true);
        File largestPhoto = null;
        List<PhotoSize> photoList = update.getMessage().getPhoto();
        photoList.sort(Comparator.comparing(PhotoSize::getFileSize));
        Collections.reverse(photoList);
        String filePath = getFilePath(photoList.get(0));
        largestPhoto = downloadPhotoByFilePath(filePath);

        if(DBUtil.isFilePresent(largestPhoto)){
            System.out.println("File already present: " + largestPhoto.getName());
            return;
        }

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
            sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath(), "Das ist ein Bon oder?", KeyboardFactory.getKeyBoard(KeyboardFactory.KeyBoardType.Boolean));
        }

        System.out.println(update.getMessage().getText());
        setBusy(false);
    }


    public void sendPhotoFromURL(Update update, String imagePath, String caption, ReplyKeyboardMarkup possibleKeyBoardOrNull){
        SendPhoto sendPhoto = null;
        try {
            sendPhoto = new SendPhoto().setPhoto("SomeText", new FileInputStream(new File(imagePath)));
            sendPhoto.setCaption(caption);
            if(possibleKeyBoardOrNull != null){
                sendPhoto.setReplyMarkup(possibleKeyBoardOrNull);
            }
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
        if(update.getMessage().getText() != null) {
            String input = update.getMessage().getText();

            String cmd = input.contains(" ") ? input.substring(0, input.indexOf(" ")).toLowerCase().replace("/", "") : input.toLowerCase().replace("/", "");

            if (cmd.startsWith("start")) {
                return new StartProcess(this, update);
            } else {
                if (cmd.startsWith("search") && input.equals("Search Document")) {
                return new SearchProcess(this, update);
            } else {
                if (cmd.startsWith("getpics") && input.equals("Get Documents")) {
                    return new GetPicsProcess(this, update);
                } else {

                    if (cmd.startsWith("getsum") && input.equals("Get Sum of Bons")) {
                        return new SumProcess(this);
                    } else {
                        if (cmd.startsWith("getbons") && input.equals("Get Bons")) {
                            return new GetBonsProcess(this);
                        } else {
                            if (cmd.startsWith("removelast") && input.equals("Remove last Document")) {
                                return new RemoveLastProcess(this);
                            }
                        }
                    }
                    }
                }
            }
        }
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

    //GETTER SETTER
    public Boolean getBusy() {
        return isBusy;
    }

    public void setBusy(Boolean busy) {
        isBusy = busy;
    }
}