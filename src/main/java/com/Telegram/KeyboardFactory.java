package com.Telegram;

import com.Utils.LogUtil;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyboardFactory {

    public static ReplyKeyboard getKeyBoard(KeyBoardType keyBoardType, boolean inlineKeyboard, boolean oneTimeKeyboard) {
        if(inlineKeyboard){
            InlineKeyboardMarkup replyKeyboardInline = new InlineKeyboardMarkup();
            replyKeyboardInline.setKeyboard(createInlineKeyboard(keyBoardType));
            if(oneTimeKeyboard){
                LogUtil.log("No oneTimeKeyboard possible for InlineKeyboard.");
            }
                return replyKeyboardInline;
        }else {
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            List<KeyboardRow> keyboard = createKeyBoard(keyBoardType);
            replyKeyboardMarkup.setKeyboard(keyboard);
            replyKeyboardMarkup.setOneTimeKeyboard(oneTimeKeyboard);
            return replyKeyboardMarkup;
        }
    }

    private static List<List<InlineKeyboardButton>> createInlineKeyboard(KeyBoardType keyBoardType){
        List<List<InlineKeyboardButton>> endKeyboard = new ArrayList<>();
        switch (keyBoardType){
            case Boolean:
                endKeyboard.add(createInlineKeyboardRow(Map.of("Japp", "confirm", "Nee", "deny")));
                break;
            case Calendar_Month:
                endKeyboard.add(createInlineKeyboardRow(Map.of("JAN","JAN", "FEB","FEB","MÄR","MÄR","APR","APR")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("MAI","MAI", "JUN","JUN","JUL","JUL","AUG","AUG")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("SEP","SEP", "OKT","OKT","NOV","NOV","DEZ","DEZ")));
                break;
            case Calendar_Year:
                endKeyboard.add(createInlineKeyboardRow(Map.of("2009","2009", "2010","2010","2011","2011","2012","2012")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("2013","2013", "2014","2014","2015","2015","2016","2016")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("2017","2017", "2018","2018","2019","2019","2020","2020")));
                break;
            case ShoppingList:
                endKeyboard.add(createInlineKeyboardRow(Map.of("Hinzufügen", "add")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Item Löschen", "deleteItem")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Ganze Liste Löschen", "deleteWholeList")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Einkaufsliste anzeigen", "showShoppingList")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Start", "start")));
                break;
            case Start:
                endKeyboard.add(createInlineKeyboardRow(Map.of("Anzahl Dokumente", "documentsCount")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Hole Bilder, Dokumente", "getDocuments")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Summe von Bons", "sumBons")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Letztes Bild Löschen", "deleteLastDocument")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Einkaufslisten-Optionen", "showShoppingListOptions")));
                endKeyboard.add(createInlineKeyboardRow(Map.of("Einkaufsliste anzeigen", "showShoppingList")));
                break;
        }
        return endKeyboard;
    }

    private static List<InlineKeyboardButton> createInlineKeyboardRow(Map<String, String> buttonNamesAndQueries){
        List<InlineKeyboardButton> inlineKeyboardButtonsRow = new ArrayList<>();
        for(String name : buttonNamesAndQueries.keySet()){
            String callBackQuery = buttonNamesAndQueries.get(name);
            inlineKeyboardButtonsRow.add(new InlineKeyboardButton().setText(name).setCallbackData(callBackQuery));
        }
        return inlineKeyboardButtonsRow;
    }

    private static List<KeyboardRow> createKeyBoard(KeyBoardType keyBoardType){
        List<KeyboardRow> keyboard = new ArrayList<>();
        switch (keyBoardType){
            case Boolean:
                KeyboardRow keyboardFirstRow = createKeyBoardRow(new String[]{"Japp", "Nee"});
                keyboard.add(keyboardFirstRow);
                break;
            case Calendar_Month:
                KeyboardRow keyboardMonthFirstRow = createKeyBoardRow(new String[]{"JAN", "FEB","MÄR","APR",});
                keyboard.add(keyboardMonthFirstRow);
                KeyboardRow keyboardMonthSecondRow = createKeyBoardRow(new String[]{"MAI", "JUN","JUL","AUG",});
                keyboard.add(keyboardMonthSecondRow);
                KeyboardRow keyboardMonthThirdRow = createKeyBoardRow(new String[]{"SEP", "OKT","NOV","DEZ",});
                keyboard.add(keyboardMonthThirdRow);
                break;
            case Calendar_Year:
                KeyboardRow keyboardYearFirstRow = createKeyBoardRow(new String[]{"2009", "2010","2011","2012",});
                keyboard.add(keyboardYearFirstRow);
                KeyboardRow keyboardYearSecondRow = createKeyBoardRow(new String[]{"2013", "2014","2015","2016",});
                keyboard.add(keyboardYearSecondRow);
                KeyboardRow keyboardYearThirdRow = createKeyBoardRow(new String[]{"2017", "2018","2019","2020",});
                keyboard.add(keyboardYearThirdRow);
                break;
                case ShoppingList:
                KeyboardRow keyboardShoppingListFirstRow = createKeyBoardRow(new String[]{"Hinzufügen"});
                keyboard.add(keyboardShoppingListFirstRow);
                KeyboardRow keyboardShoppingListSecondRow = createKeyBoardRow(new String[]{"Item Löschen"});
                keyboard.add(keyboardShoppingListSecondRow);
                KeyboardRow keyboardShoppingListThirdRow = createKeyBoardRow(new String[]{"Ganze Liste Löschen"});
                keyboard.add(keyboardShoppingListThirdRow);
                KeyboardRow keyboardShoppingListFourthRow = createKeyBoardRow(new String[]{"Einkaufsliste anzeigen"});
                keyboard.add(keyboardShoppingListFourthRow);
                KeyboardRow keyboardShoppingListFifthRow = createKeyBoardRow(new String[]{"Start"});
                keyboard.add(keyboardShoppingListFifthRow);
                break;
            case Start:
                KeyboardRow keyboardStartFirstRow = createKeyBoardRow(new String[]{"Bon eingeben"});
                keyboard.add(keyboardStartFirstRow);
                KeyboardRow keyboardStartSecondRow = createKeyBoardRow(new String[]{"Hole Bilder, Dokumente"});
                keyboard.add(keyboardStartSecondRow);
                KeyboardRow keyboardStartThirdRow = createKeyBoardRow(new String[]{"Summe von Bons"});
                keyboard.add(keyboardStartThirdRow);
                KeyboardRow keyboardStartFourthRow = createKeyBoardRow(new String[]{"Hole Bons"});
                keyboard.add(keyboardStartFourthRow);
                KeyboardRow keyboardStartFifthRow = createKeyBoardRow(new String[]{"Letztes Bild Löschen"});
                keyboard.add(keyboardStartFifthRow);
                KeyboardRow keyboardStartSeventhRow = createKeyBoardRow(new String[]{"Einkaufslisten-Optionen"});
                keyboard.add(keyboardStartSeventhRow);
                KeyboardRow keyboardStartEigthRow = createKeyBoardRow(new String[]{"Einkaufsliste anzeigen"});
                keyboard.add(keyboardStartEigthRow);
                break;
        }
        return keyboard;
    }

    public enum KeyBoardType{
        Boolean, Calendar_Month, Calendar_Year, Start, ShoppingList
    }
    private static KeyboardRow createKeyBoardRow(String[] namesOfButtons){
        KeyboardRow keyboardRow = new KeyboardRow();
        for(String name : namesOfButtons){
            keyboardRow.add(name);
        }
        return keyboardRow;
    }
}
