package com.Telegram.Processes;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
import com.ObjectTemplates.Image;
import com.ObjectTemplates.User;
import com.Telegram.Bot;
import com.Telegram.Item;
import com.Telegram.KeyboardFactory;
import com.Utils.DBUtil;
import com.Utils.LogUtil;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StandardListProcess extends Process {

    private File picturesFolder;

    private List<Item> standardList;

    private String action = null;

    private Item item = null;

    private int index = 0;

    public StandardListProcess(Bot bot, Update update, ProgressReporter progressReporter, Map<Integer, User> allowedUsersMap){
        super(progressReporter);
        setBot(bot);
        standardList = DBUtil.getStandardListFromDB();
        checkForPictureFolder();
        performNextStep("asd", update, allowedUsersMap);
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        //Terms in this set need more userinformation in a further step
        Set<String> commandsWithLaterExecution = Set.of("Item hinzufügen", "Item löschen");
        if(action != null){
            if(action.equals("removeitem")){
                removeItem(update);
            }else{
                if(action.equals("add")){
                    if(!update.getMessage().hasPhoto() || update.getMessage().getCaption() == null || update.getMessage().getCaption().equals("")){
                        getBot().sendMsg("Kein Bild oder Name für das Item dabei :/", update, KeyboardFactory.KeyBoardType.Abort, true,true);
                        setAwaitsInput(true);
                    }else {
                        List<PhotoSize> photoList = update.getMessage().getPhoto();
                        photoList.sort(Comparator.comparing(PhotoSize::getFileSize));
                        Collections.reverse(photoList);
                        String filePath = getBot().getFilePath(photoList.get(0));
                        File largestPhoto = getBot().downloadPhotoByFilePath(filePath);
                        File newPhoto = new File(picturesFolder, largestPhoto.getName());
                        try {
                            FileUtils.copyFile(largestPhoto, newPhoto);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        item = new Item(update.getMessage().getCaption(), newPhoto);
                        standardList.add(item);
                        DBUtil.executeSQL("insert into StandardList(item, picturePath) Values ('" + item.getName() + "', '" + item.getPicturePath() + "')");
                        Message message = getBot().sendMsg(item.getName() + " hinzugefügt! :)", update, KeyboardFactory.KeyBoardType.Done, false, true);
                        getSentMessages().add(message);
                        close();
                        return;
                    }
                }
            }
        }
        if(getBot().getMassageFromUpdate(update).hasText() && !commandsWithLaterExecution.contains(getBot().getMassageFromUpdate(update).getText())){
            processInOneStep(arg, update, allowedUsersMap);
        }else{Message message = getBot().getMassageFromUpdate(update);
            switch (arg) {
                case "done":
                    this.close();
                    break;
                case "<<":
                    index = 0;
                    getBot().sendOrEditSLIDESHOWMESSAGE(message.getText(), standardList.get(index), update);
                    item = standardList.get(index);
                    break;
                case "<":
                    index = index != 0 ? index - 1 : 0;
                    getBot().sendOrEditSLIDESHOWMESSAGE(message.getText(), standardList.get(index), update);
                    item = standardList.get(index);
                    break;
                case "select":
                    removeItem(update);
                    break;
                case ">":
                    index = index == standardList.size() - 1 ? standardList.size() - 1 : index + 1;
                    getBot().sendOrEditSLIDESHOWMESSAGE(message.getText(), standardList.get(index), update);
                    item = standardList.get(index);
                    break;
                case ">>":
                    index = standardList.size() - 1;
                    getBot().sendOrEditSLIDESHOWMESSAGE(message.getText(), standardList.get(index), update);
                    item = standardList.get(index);
                    break;
                default:
                    prepareForProcessing(update);
                    break;
            }
        }
        getBot().setBusy(false);
    }
    private void removeItem(Update update){
        try{
            DBUtil.executeSQL("delete from StandardList where item='" +  item.getName() + "'");
            int indexOfItem = standardList.indexOf(item);
            standardList.remove(item);
            getBot().sendAnswerCallbackQuery(item.getName() + " gelöscht.", false, update.getCallbackQuery());
getBot().sendOrEditSLIDESHOWMESSAGE("Gelöscht. Noch was?", indexOfItem == 0 ? standardList.get(0) : standardList.get( indexOfItem- 1), update);
        }catch (Exception e){
            LogUtil.logError(null, e);
        }
    }

    private void prepareForProcessing(Update update) {
        Message message = getBot().getMassageFromUpdate(update);
        switch (message.getText()){
            case "Item hinzufügen":
                message = getBot().sendMsg("Was soll hinzugefügt werden?", update, KeyboardFactory.KeyBoardType.Abort, false, true);
                action = "add";
                setAwaitsInput(true);
                break;
            case "Item löschen":
                List<String> standardItemNames = new ArrayList<>();
                ReplyKeyboard shoppingListKeyboard = KeyboardFactory.getInlineKeyboardForList(standardItemNames);
                index = index > standardList.size()-1 ? standardList.size() - 1 : index;
                message = getBot().sendOrEditSLIDESHOWMESSAGE("Was soll gelöscht werden?", standardList.get(index), update);
                item = standardList.get(index);
                break;
        }
        getSentMessages().add(message);
    }

    private void processInOneStep(String arg, Update update, Map<Integer, User> allowedUsersMap) {
        String input = null;
        String cmd = arg;
        if(arg.equals("done")){
            input = "done";
        }else{
            if(item != null ){
                input = action + " " + item.getName();
            }else{
                input = getBot().getMassageFromUpdate(update).getText();
            }}

        switch (input){
            case "done":
                getBot().sendMsg("Ok :)", update, null, false, false);
                this.close();
                break;
            case "Standardliste anzeigen":
                StringBuilder stringBuilder = new StringBuilder("Aktuelle Standardliste:\n");
                DBUtil.getStandardListFromDB().forEach(item1 -> stringBuilder.append(item1.getName() + "\n"));
                getBot().sendMsg(stringBuilder.toString(), update, KeyboardFactory.KeyBoardType.NoButtons, false, true);
                close();
                break;
            default:
                if((input.contains("add") || input.contains("removeitem"))){
                    cmd = input.substring(0, input.indexOf(" ")).toLowerCase();
                }
                break;
        }
    }

    @Override
    public String getProcessName() {
        return "StandardList";
    }

    @Override
    public void close(){
        this.clearButtons();
        setDeleteLater(true);
    }

    @Override
    public void clearButtons(){
        for(Message message : getSentMessages()){
            if(message != null){
                    getBot().simpleEditMessage(message.getText(), message, KeyboardFactory.KeyBoardType.NoButtons);
            }}
    }
    private void checkForPictureFolder(){
        picturesFolder = new File(ObjectHub.getInstance().getArchiver().getResourceFolder(), "StandardListPictures");
        if(!picturesFolder.exists()){
            picturesFolder.mkdir();
        }
    }
}
