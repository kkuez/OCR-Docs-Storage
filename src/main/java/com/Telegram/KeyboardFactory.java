package com.Telegram;

import com.Utils.DBUtil;
import com.Utils.LogUtil;
import com.Utils.TimeUtil;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyboardFactory {

    private static List<InlineKeyboardButton> DONE_ROW = createInlineKeyboardRow(Map.of("Fertig", "done"));

    private static List<InlineKeyboardButton> ABORT_ROW = createInlineKeyboardRow(Map.of("Abbrechen", "abort"));


    public static ReplyKeyboard getKeyBoard(KeyBoardType keyBoardType, boolean inlineKeyboard, boolean oneTimeKeyboard, String callbackValuePrefix) {
        if(inlineKeyboard){
            InlineKeyboardMarkup replyKeyboardInline = new InlineKeyboardMarkup();
            replyKeyboardInline.setKeyboard(createInlineKeyboard(keyBoardType, callbackValuePrefix));
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

    public static List<List<InlineKeyboardButton>> createInlineKeyboard(KeyBoardType keyBoardType, String valuePrefixOrNull){
        List<List<InlineKeyboardButton>> endKeyboard = new ArrayList<>();
        switch (keyBoardType){
            case Done:
                endKeyboard.add(DONE_ROW);
                break;
            case Abort:
                endKeyboard.add(ABORT_ROW);
                break;
            case Boolean:
                endKeyboard.add(createInlineKeyboardRow(Map.of("Japp", "confirm", "Nee", "deny")));
                endKeyboard.add(ABORT_ROW);
                break;
            case Calendar_Month:
                endKeyboard.add(createInlineKeyboardRow(List.of("JAN", "FEB","MÄR","APR"), List.of(valuePrefixOrNull + "JAN", valuePrefixOrNull + "FEB",valuePrefixOrNull + "MÄR",valuePrefixOrNull + "APR")));
                endKeyboard.add(createInlineKeyboardRow(List.of("MAI", "JUN","JUL","AUG"), List.of(valuePrefixOrNull + "MAI",valuePrefixOrNull +  "JUN",valuePrefixOrNull + "JUL",valuePrefixOrNull + "AUG")));
                endKeyboard.add(createInlineKeyboardRow(List.of("SEP", "OKT","NOV","DEZ"), List.of(valuePrefixOrNull + "SEP",valuePrefixOrNull +  "OKT",valuePrefixOrNull + "NOV",valuePrefixOrNull + "DEZ")));
                endKeyboard.add(ABORT_ROW);
                break;
            case Calendar_Year:
                endKeyboard.add(createInlineKeyboardRow(List.of("2009", "2010","2011","2012"), List.of(valuePrefixOrNull + "2009", valuePrefixOrNull + "2010",valuePrefixOrNull + "2011",valuePrefixOrNull + "2012")));
                endKeyboard.add(createInlineKeyboardRow(List.of("2013","2014","2015","2016"), List.of(valuePrefixOrNull + "2013",valuePrefixOrNull  + "2014",valuePrefixOrNull + "2015",valuePrefixOrNull + "2016")));
                endKeyboard.add(createInlineKeyboardRow(List.of("2017", "2018","2019","2020"), List.of(valuePrefixOrNull + "2017", valuePrefixOrNull  + "2018",valuePrefixOrNull + "2019",valuePrefixOrNull + "2020")));
                endKeyboard.add(ABORT_ROW);
                break;
            case ShoppingList_Current:
                List<String> shoppingList = DBUtil.getShoppingListFromDB();
                shoppingList.forEach(item -> endKeyboard.add(createInlineKeyboardRow(Map.of(item, valuePrefixOrNull + item))));
                endKeyboard.add(DONE_ROW);
                break;
            case ShoppingList_Add:
                endKeyboard.add(createInlineKeyboardRow(Map.of("Standardliste anzeigen", "Standardliste anzeigen")));
                endKeyboard.add(DONE_ROW);
                break;
            case StandardList_Current:
                List<String> standardList = DBUtil.getStandardListFromDB();
                standardList.forEach(item -> endKeyboard.add(createInlineKeyboardRow(Map.of(item, valuePrefixOrNull + item))));
                endKeyboard.add(DONE_ROW);
                break;
            case Calendar_Choose_Strategy:
                endKeyboard.add(createInlineKeyboardRow(List.of("Einmaliger Termin"), List.of("chooseStrategyoneTime")));
                endKeyboard.add(createInlineKeyboardRow(List.of("Regelmäßiger Termin"), List.of("chooseStrategyregular")));
                endKeyboard.add(ABORT_ROW);
                break;
            case Calendar_Regular_Choose_Unit:
                endKeyboard.add(createInlineKeyboardRow(List.of("Täglich"), List.of("daily")));
                endKeyboard.add(createInlineKeyboardRow(List.of("Monatlich"), List.of("monthly")));
                endKeyboard.add(createInlineKeyboardRow(List.of("Jährlich"), List.of("yearly")));
                endKeyboard.add(ABORT_ROW);
                break;
            case User_Choose:
                endKeyboard.add(createInlineKeyboardRow(List.of("Für mich"), List.of("forMe")));
                endKeyboard.add(createInlineKeyboardRow(List.of("Für Alle"), List.of("forAll")));
                endKeyboard.add(ABORT_ROW);
                break;
        }
        return endKeyboard;
    }

    private static List<InlineKeyboardButton> createInlineKeyboardRow(List<String> keyList, List<String> valueList){
        //For ordered processing
        List<InlineKeyboardButton> inlineKeyboardButtonsRow = new ArrayList<>();
        for(int i = 0; i < valueList.size(); i++){
            String callBackQuery = valueList.get(i);
            inlineKeyboardButtonsRow.add(new InlineKeyboardButton().setText(keyList.get(i)).setCallbackData(callBackQuery));
        }
        return inlineKeyboardButtonsRow;
    }

    private static List<InlineKeyboardButton> createInlineKeyboardRow(Map<String, String> buttonNamesAndQueries){
    List<String> keyList = new ArrayList<>();
    List<String> valueList = new ArrayList<>();
    for(String key : buttonNamesAndQueries.keySet()){
        keyList.add(key);
        valueList.add(buttonNamesAndQueries.get(key));
    }
    return createInlineKeyboardRow(keyList, valueList);
    }

    public static ReplyKeyboard getInlineKeyboardForList(List<String> list, String valuePrefixOrNull){
        String prefix = valuePrefixOrNull != null ? valuePrefixOrNull : "";
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> endKeyboard = new ArrayList<>();
        for(String item : list){
            List<InlineKeyboardButton> buttonRows = createInlineKeyboardRow(Map.of(item, prefix + item));
            endKeyboard.add(buttonRows);
        }
        endKeyboard.add(DONE_ROW);
        inlineKeyboardMarkup.setKeyboard(endKeyboard);
        return inlineKeyboardMarkup;
    }

    public static List<List<InlineKeyboardButton>> createInlineKeyboardForYearMonth(int year, int month){
        List<List<InlineKeyboardButton>> endKeyboard = new ArrayList<>();
        List<Integer> numberOfDays = TimeUtil.getDaysInMonthOfYear(year).get(month);
        List<String> daysInLastRowList = new ArrayList<>();
        for(Integer dayNumber: numberOfDays){
            if(dayNumber > 28){
                daysInLastRowList.add(dayNumber + "");
            }
        }
        List<String> firstRowKeys = List.of("1", "2", "3", "4", "5", "6", "7");
        List<String> firstRowValues = makeValueList(firstRowKeys, "day");
        List<String> secondRowKeys = List.of("8", "9", "10", "11", "12", "13", "14");
        List<String> secondRowValues = makeValueList(secondRowKeys, "day");
        List<String> thirdRowKeys = List.of("15", "16", "17", "18", "19", "20", "21");
        List<String> thirdRowValues = makeValueList(thirdRowKeys, "day");
        List<String> fourthRowKeys = List.of("22", "23", "24", "25", "26", "27", "28");
        List<String> fourthRowValues = makeValueList(fourthRowKeys, "day");
        List<String> fifthRowKeys = daysInLastRowList;
        List<String> fifthRowValues = makeValueList(fifthRowKeys, "day");
        endKeyboard.add(createInlineKeyboardRow(firstRowKeys, firstRowValues));
        endKeyboard.add(createInlineKeyboardRow(secondRowKeys, secondRowValues));
        endKeyboard.add(createInlineKeyboardRow(thirdRowKeys, thirdRowValues));
        endKeyboard.add(createInlineKeyboardRow(fourthRowKeys, fourthRowValues));
        endKeyboard.add(createInlineKeyboardRow(fifthRowKeys, fifthRowValues));
        return endKeyboard;
    }

//TODO die Methode überall implementieren
    private static List<String> makeValueList(List<String> keyList, String prefix){
List<String> valueList = new ArrayList<>();
for (String key: keyList){
    valueList.add(prefix + key);
}
return valueList;

    }

    private static List<KeyboardRow> createKeyBoard(KeyBoardType keyBoardType){
        List<KeyboardRow> keyboard = new ArrayList<>();
        switch (keyBoardType){
            case NoButtons:
                break;
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
                KeyboardRow keyboardShoppingListFirstRow = createKeyBoardRow(new String[]{"Hinzufügen", "Löschen"});
                keyboard.add(keyboardShoppingListFirstRow);
                KeyboardRow keyboardShoppingListSecondRow = createKeyBoardRow(new String[]{"Liste anzeigen", "Liste Löschen"});
                keyboard.add(keyboardShoppingListSecondRow);
                KeyboardRow keyboardShoppingListThirdRow = createKeyBoardRow(new String[]{"Standardliste: Optionen", "Start"});
                keyboard.add(keyboardShoppingListThirdRow);
                break;
            case StandardList:
                KeyboardRow keyboardStandardListFirstRow = createKeyBoardRow(new String[]{"Hinzufügen", "Löschen"});
                keyboard.add(keyboardStandardListFirstRow);
                KeyboardRow keyboardStandardListSecondRow = createKeyBoardRow(new String[]{"Liste anzeigen"});
                keyboard.add(keyboardStandardListSecondRow);
                KeyboardRow keyboardStandardListThirdRow = createKeyBoardRow(new String[]{"Start"});
                keyboard.add(keyboardStandardListThirdRow);
                break;
            case Start:
                KeyboardRow keyboardStartFirstRow = createKeyBoardRow(new String[]{"Bon eingeben"});
                keyboard.add(keyboardStartFirstRow);
                KeyboardRow keyboardStartEigthRow = createKeyBoardRow(new String[]{"Liste anzeigen"});
                keyboard.add(keyboardStartEigthRow);
                KeyboardRow keyboardStartSecondRow = createKeyBoardRow(new String[]{"Hole Bilder, Dokumente"});
                keyboard.add(keyboardStartSecondRow);
                KeyboardRow keyboardStartFifthRow = createKeyBoardRow(new String[]{"Letztes Bild Löschen"});
                keyboard.add(keyboardStartFifthRow);
                KeyboardRow keyboardStartFourthRow = createKeyBoardRow(new String[]{"Bon-Optionen"});
                keyboard.add(keyboardStartFourthRow);
                KeyboardRow keyboardStartSeventhRow = createKeyBoardRow(new String[]{"Einkaufslisten-Optionen"});
                keyboard.add(keyboardStartSeventhRow);
                KeyboardRow keyboardStartEightRow = createKeyBoardRow(new String[]{"Kalender-Optionen"});
                keyboard.add(keyboardStartEightRow);
                break;
            case Bons:
                KeyboardRow keyboardBonsFirstRow = createKeyBoardRow(new String[]{"Bon eingeben"});
                keyboard.add(keyboardBonsFirstRow);
                KeyboardRow keyboardBonsSecondRow = createKeyBoardRow(new String[]{"Summe von Bons"});
                keyboard.add(keyboardBonsSecondRow);
                KeyboardRow keyboardBonsThirdRow = createKeyBoardRow(new String[]{"Hole Bons"});
                keyboard.add(keyboardBonsThirdRow);
                KeyboardRow keyboardBonsFourthRow = createKeyBoardRow(new String[]{"Start"});
                keyboard.add(keyboardBonsFourthRow);
                break;
            case Calendar:
                KeyboardRow keyboardCalendarFirstRow = createKeyBoardRow(new String[]{"Termine anzeige"});
                keyboard.add(keyboardCalendarFirstRow);
                KeyboardRow keyboardCalendarSecondRow = createKeyBoardRow(new String[]{"Termin hinzufügen"});
                keyboard.add(keyboardCalendarSecondRow);
                KeyboardRow keyboardCalendarThirdRow = createKeyBoardRow(new String[]{"Termin löschen"});
                keyboard.add(keyboardCalendarThirdRow);
                KeyboardRow keyboardCalendarFourthRow = createKeyBoardRow(new String[]{"Start"});
                keyboard.add(keyboardCalendarFourthRow);
                break;
        }
        return keyboard;
    }

    public enum KeyBoardType{
        User_Choose, Boolean,  Calendar, Calendar_Month, Calendar_Year, Calendar_Choose_Strategy, Calendar_Regular_Choose_Unit, Start, ShoppingList, ShoppingList_Current, ShoppingList_Add, Abort, Bons, NoButtons, Done, StandardList, StandardList_Current
    }
    private static KeyboardRow createKeyBoardRow(String[] namesOfButtons){
        KeyboardRow keyboardRow = new KeyboardRow();
        for(String name : namesOfButtons){
            keyboardRow.add(name);
        }
        return keyboardRow;
    }
}
