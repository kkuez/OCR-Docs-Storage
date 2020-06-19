package com.bot.telegram;

import com.Main;
import com.utils.DBUtil;

import com.utils.TimeUtil;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class KeyboardFactory {
    private static Logger logger = Main.getLogger();

    private static List<InlineKeyboardButton> DONE_ROW = createInlineKeyboardRow(Map.of("Fertig", "done"));

    private static List<InlineKeyboardButton> ABORT_ROW = createInlineKeyboardRow(Map.of("Abbrechen", "abort"));

    public static ReplyKeyboard getKeyBoard(KeyBoardType keyBoardType, boolean inlineKeyboard, boolean oneTimeKeyboard, String callbackValuePrefix) {
        if(inlineKeyboard){
            InlineKeyboardMarkup replyKeyboardInline = new InlineKeyboardMarkup();
            replyKeyboardInline.setKeyboard(createInlineKeyboard(keyBoardType, callbackValuePrefix));
            if(oneTimeKeyboard){
                logger.info("No oneTimeKeyboard possible for InlineKeyboard.");
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
                endKeyboard.add(createInlineKeyboardRow(List.of("2020", "2021","2022","2023"), List.of(valuePrefixOrNull + "2020", valuePrefixOrNull + "2021",valuePrefixOrNull + "2022",valuePrefixOrNull + "2023")));
                endKeyboard.add(createInlineKeyboardRow(List.of("2024","2025","2026","2027"), List.of(valuePrefixOrNull + "2024",valuePrefixOrNull  + "2026",valuePrefixOrNull + "2027",valuePrefixOrNull + "2028")));
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
                endKeyboard.add(createInlineKeyboardRow(List.of("Einmaliger Termin mit Uhrzeit"), List.of("chooseStrategyoneTimeWithTime")));
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
            case QRItems:
                Map<Integer, String> itemsMap = DBUtil.getQRItemMap();
                int itemCount = itemsMap.size();
                for(int i = 1;i<itemCount + 1;i++){
                    endKeyboard.add(createInlineKeyboardRow(List.of("Item" + i + ": " + itemsMap.get(i)), List.of("" + i)));
                }
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
        List<Integer> daysInMonth = TimeUtil.getDaysInMonthOfYear(year).get(month);
        List<String> daysInLastRowList = new ArrayList<>();
        for(Integer dayNumber: daysInMonth){
            if(dayNumber > 28){
                daysInLastRowList.add(dayNumber + "");
            }
        }

        DayOfWeek firstDayOfWeek = LocalDate.of(year, month, 1).getDayOfWeek();

        List<String> rowKeys = new ArrayList<>(7);
        for(DayOfWeek dayOfWeek: DayOfWeek.values()) {
            if(firstDayOfWeek.equals(dayOfWeek)) {
                break;
            }
            rowKeys.add("-");
        }

        while(rowKeys.size() != 7) {
            rowKeys.add(daysInMonth.get(0) + "");
            daysInMonth.remove(0);

        }

        List<String> rowValues = makeValueList(rowKeys, "chooseDay");
        endKeyboard.add(createInlineKeyboardRow(rowKeys, rowValues));


        while(daysInMonth.size() > 0) {
        rowKeys = new ArrayList<>(7);
        rowValues = new ArrayList<>(7);

            while(rowKeys.size() != 7) {
                String dayInMonth = daysInMonth.size() > 0 ? daysInMonth.get(0) + "" : "-";
                rowKeys.add(dayInMonth);
                if(daysInMonth.size() > 0) {
                    daysInMonth.remove(0);
                }
            }
            rowValues = makeValueList(rowKeys, "chooseDay");
            endKeyboard.add(createInlineKeyboardRow(rowKeys, rowValues));
        }

        return endKeyboard;
    }

    public static List<List<InlineKeyboardButton>> createInlineKeyboardForHour(){
        List<List<InlineKeyboardButton>> endKeyboard = new ArrayList<>();
        List<String> firstRowKeys = List.of("1", "2", "3", "4", "5", "6");
        List<String> firstRowValues = makeValueList(firstRowKeys, "chooseHour");
        List<String> secondRowKeys = List.of("7", "8", "9", "10", "11", "12");
        List<String> secondRowValues = makeValueList(secondRowKeys, "chooseHour");
        List<String> thirdRowKeys = List.of("13", "14", "15", "16", "17", "18");
        List<String> thirdRowValues = makeValueList(thirdRowKeys, "chooseHour");
        List<String> fourthRowKeys = List.of("19", "20", "21", "22", "23", "24");
        List<String> fourthRowValues = makeValueList(fourthRowKeys, "chooseHour");
        endKeyboard.add(createInlineKeyboardRow(firstRowKeys, firstRowValues));
        endKeyboard.add(createInlineKeyboardRow(secondRowKeys, secondRowValues));
        endKeyboard.add(createInlineKeyboardRow(thirdRowKeys, thirdRowValues));
        endKeyboard.add(createInlineKeyboardRow(fourthRowKeys, fourthRowValues));
        return endKeyboard;
    }

    public static List<List<InlineKeyboardButton>> createInlineKeyboardForMinute(){
        List<List<InlineKeyboardButton>> endKeyboard = new ArrayList<>();
        List<String> firstRowKeys = List.of("0", "5", "10", "15");
        List<String> firstRowValues = makeValueList(firstRowKeys, "chooseMinute");
        List<String> secondRowKeys = List.of("20", "25", "30", "35");
        List<String> secondRowValues = makeValueList(secondRowKeys, "chooseMinute");
        List<String> thirdRowKeys = List.of("40", "45", "50", "55");
        List<String> thirdRowValues = makeValueList(thirdRowKeys, "chooseMinute");
        endKeyboard.add(createInlineKeyboardRow(firstRowKeys, firstRowValues));
        endKeyboard.add(createInlineKeyboardRow(secondRowKeys, secondRowValues));
        endKeyboard.add(createInlineKeyboardRow(thirdRowKeys, thirdRowValues));
        return endKeyboard;
    }

    //TODO die Methode überall implementieren
    private static List<String> makeValueList(List<String> keyList, String prefix){
        List<String> valueList = new ArrayList<>();
        for (String key: keyList){
            valueList.add(key == "-" ? prefix + "-" : prefix + key);
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
                KeyboardRow keyboardShoppingListSecondRow = createKeyBoardRow(new String[]{"Einkaufsliste anzeigen", "Liste Löschen"});
                keyboard.add(keyboardShoppingListSecondRow);
                KeyboardRow keyboardShoppingListThirdRow = createKeyBoardRow(new String[]{"Standardliste: Optionen", "Start"});
                keyboard.add(keyboardShoppingListThirdRow);
                break;
            case StandardList:
                KeyboardRow keyboardStandardListFirstRow = createKeyBoardRow(new String[]{"Hinzufügen", "Löschen"});
                keyboard.add(keyboardStandardListFirstRow);
                KeyboardRow keyboardStandardListSecondRow = createKeyBoardRow(new String[]{"Standardliste anzeigen"});
                keyboard.add(keyboardStandardListSecondRow);
                KeyboardRow keyboardStandardListThirdRow = createKeyBoardRow(new String[]{"Start"});
                keyboard.add(keyboardStandardListThirdRow);
                break;
            case Start:
                KeyboardRow keyboardStartFirstRow = createKeyBoardRow(new String[]{"Bon eingeben"});
                keyboard.add(keyboardStartFirstRow);
                KeyboardRow keyboardStartEigthRow = createKeyBoardRow(new String[]{"Einkaufsliste anzeigen"});
                keyboard.add(keyboardStartEigthRow);
                KeyboardRow keyboardStartSecondRow = createKeyBoardRow(new String[]{"Zu Einkaufsliste hinzufügen"});
                keyboard.add(keyboardStartSecondRow);
                KeyboardRow keyboardStartThirdRow = createKeyBoardRow(new String[]{"Kalender"});
                keyboard.add(keyboardStartThirdRow);
                KeyboardRow keyboardStartFourthRow = createKeyBoardRow(new String[]{"Letztes Bild Löschen"});
                keyboard.add(keyboardStartFourthRow);
                KeyboardRow keyboardStartFifthRow = createKeyBoardRow(new String[]{"Weitere Optionen"});
                keyboard.add(keyboardStartFifthRow);
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
            case Memo:
                KeyboardRow keyboardMemoFirstRow = createKeyBoardRow(new String[]{"Memos anzeigen"});
                keyboard.add(keyboardMemoFirstRow);
                KeyboardRow keyboardMemoSecondRow = createKeyBoardRow(new String[]{"Memo hinzufügen"});
                keyboard.add(keyboardMemoSecondRow);
                KeyboardRow keyboardMemoThirdRow = createKeyBoardRow(new String[]{"Memos löschen"});
                keyboard.add(keyboardMemoThirdRow);
                KeyboardRow keyboardMemoFourthRow = createKeyBoardRow(new String[]{"Start"});
                keyboard.add(keyboardMemoFourthRow);
                break;
            case FurtherOptions:
                KeyboardRow keyboardFurtherFirstRow = createKeyBoardRow(new String[]{"QR-Item mappen"});
                keyboard.add(keyboardFurtherFirstRow);
                KeyboardRow keyboardFurtherSecondRow = createKeyBoardRow(new String[]{"Bon-Optionen"});
                keyboard.add(keyboardFurtherSecondRow);
                KeyboardRow keyboardFurtherThirdRow = createKeyBoardRow(new String[]{"Einkaufslisten-Optionen"});
                keyboard.add(keyboardFurtherThirdRow);
                KeyboardRow keyboardFurtherFourthRow = createKeyBoardRow(new String[]{"Memo-Optionen"});
                keyboard.add(keyboardFurtherFourthRow);
                KeyboardRow keyboardFurtherFifthRow = createKeyBoardRow(new String[]{"Dokumente suchen"});
                keyboard.add(keyboardFurtherFifthRow);
                KeyboardRow keyboardFurtherSixthRow = createKeyBoardRow(new String[]{"Start"});
                keyboard.add(keyboardFurtherSixthRow);
                break;
        }
        return keyboard;
    }

    public enum KeyBoardType{
        FurtherOptions, QRItems, Memo, User_Choose, Boolean,  Calendar, Calendar_Month, Calendar_Year, Calendar_Choose_Strategy, Calendar_Regular_Choose_Unit, Start, ShoppingList, ShoppingList_Current, ShoppingList_Add, Abort, Bons, NoButtons, Done, StandardList, StandardList_Current
    }
    private static KeyboardRow createKeyBoardRow(String[] namesOfButtons){
        KeyboardRow keyboardRow = new KeyboardRow();
        for(String name : namesOfButtons){
            keyboardRow.add(name);
        }
        return keyboardRow;
    }
}
