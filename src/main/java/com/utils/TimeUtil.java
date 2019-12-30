package com.utils;

import com.Main;
import org.apache.log4j.Logger;

import java.time.YearMonth;
import java.util.*;

public class TimeUtil {
    private static Logger logger = Main.logger;

    static Map<String, String> monthMap = new HashMap<>();

    public static void waitUntilObjectsEqual(Object o1, Object o2) throws InterruptedException {
        while(!o1.equals(o2)){
                Thread.sleep(300);
        }
    }

    public static Map<Integer, List<Integer>> getDaysInMonthOfYear(int year){
        Map<Integer, List<Integer>> monthMap = new HashMap<>();
        //loops start with 1 since 1 is the first day, dont wanna have 0 in our lists.
        for(int i = 1;i < 13;i++){
            monthMap.putIfAbsent(i, new ArrayList<>());
            for(int j = 1;j < getdaysOfMonthCount(year, i) + 1;j++){
                monthMap.get(i).add(j);
            }
        }
        return monthMap;
    }
    public static int getdaysOfMonthCount(int year, int month){
        YearMonth yearMonthObject = YearMonth.of(year, month);
        return yearMonthObject.lengthOfMonth();
    }

    public static Map<Integer, String> getMonthMapIntKeys() {
        Map<Integer, String> returnMap = new HashMap<>();
        String[] monthArray = new String[]{"JAN", "FEB","MÄR","APR","MAI", "JUN","JUL","AUG","SEP", "OKT","NOV","DEZ"};
        for(int i =1; i<13;i++){
            String addZeroOrNot = i< 10 ? "0" : "";
            returnMap.put(i, monthArray[i - 1]);
        }
        return returnMap;
    }

    public static Map<String, String> getMonthMapStringKeys() {
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
    //GETTER SETTER
}