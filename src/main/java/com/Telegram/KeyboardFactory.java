package com.Telegram;

import com.Utils.BotUtil;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class KeyboardFactory {

    public static ReplyKeyboardMarkup getKeyBoard(KeyBoardType keyBoardType) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = createKeyBoard(keyBoardType);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
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
            case Start:
                KeyboardRow keyboardStartFirstRow = createKeyBoardRow(new String[]{"Search Document"});
                keyboard.add(keyboardStartFirstRow);
                KeyboardRow keyboardStartSecondRow = createKeyBoardRow(new String[]{"Get Documents"});
                keyboard.add(keyboardStartSecondRow);
                KeyboardRow keyboardStartThirdRow = createKeyBoardRow(new String[]{"Get Sum of Bons"});
                keyboard.add(keyboardStartThirdRow);
                KeyboardRow keyboardStartFourthRow = createKeyBoardRow(new String[]{"Get Bons"});
                keyboard.add(keyboardStartFourthRow);
                KeyboardRow keyboardStartFifthRow = createKeyBoardRow(new String[]{"Remove last Document"});
                keyboard.add(keyboardStartFifthRow);
                break;
        }

        return keyboard;
    }

    public enum KeyBoardType{
        Boolean, Calendar_Month, Calendar_Year, Start
    }
    private static KeyboardRow createKeyBoardRow(String[] namesOfButtons){
        KeyboardRow keyboardRow = new KeyboardRow();
        for(String name : namesOfButtons){
            keyboardRow.add(name);
        }
        return keyboardRow;
    }
}
