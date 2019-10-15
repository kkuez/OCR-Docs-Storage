package com.Utils;

import com.Misc.EqualStrategy;
import com.ObjectHub;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.util.*;

public class TimeUtil {

    static Map<String, String> monthMap = new HashMap<>();

    //GETTER SETTER
    public static Map<String, String> getMonthMap() {
        String[] monthArray =new String[]{"JAN", "FEB","MÄR","APR","MAI", "JUN","JUL","AUG","SEP", "OKT","NOV","DEZ"};
        for(int i =1; i<13;i++){
            String addZeroOrNot = i< 10 ? "0" : "";

            monthMap.put(monthArray[i - 1], addZeroOrNot + i);
        }
        return monthMap;
    }
    public static Set<String> getYearsSet() {
        String[] monthArray =new String[]{"2006","2007","2008","2009","2010","2011","2012","2013","2014","2015","2016","2017","2018","2019","2020"};
        Set<String> strings = new HashSet<>();
        strings.addAll(Arrays.asList(monthArray));
        return strings;
    }

    public static void waitUntilObjectsEqual(Object o1, Object o2){
        while(!o1.equals(o2)){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

public static void waitUntilObjectsEqualWithStrategies(Object o1, Object o2, EqualStrategy equalStrategy){

        Object[] objectsToEqual = equalStrategy.fetchObjectsToEqual(o1, o2);
        while(!objectsToEqual[0].equals(objectsToEqual[1])){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            objectsToEqual = equalStrategy.fetchObjectsToEqual(o1, o2);
        }
    }

}