package com.Misc.TaskHandling.Strategies;

public class OneTimeTaskStrategy implements TaskStrategy {

    int minuteToPerform = 99;

    int hourToPerform = 99;

    String dateToPerform;

    public OneTimeTaskStrategy(){}

    public OneTimeTaskStrategy(int minuteToPerform, int hourToPerform, String dateToPerform){
        this.minuteToPerform = minuteToPerform;
        this.hourToPerform = hourToPerform;
        this.dateToPerform = dateToPerform;
    }

    public OneTimeTaskStrategy( int hourToPerform, String dateToPerform){
        this.hourToPerform = hourToPerform;
        this.dateToPerform = dateToPerform;
    }

    public OneTimeTaskStrategy(String dateToPerform){
        this.dateToPerform = dateToPerform;
    }

    @Override
    public boolean performNow(int currentMinute, int currentHour, String currentDate) {
        if(dateToPerform.equals(currentDate)){
            if(minuteToPerform == 99 && hourToPerform == 99){
                doAfterExecute();
                return true;
            }else{
                if(hourToPerform == currentHour){
                    if(minuteToPerform == 99){
                        doAfterExecute();
                        return true;
                    }else{
                        if(minuteToPerform == currentMinute){
                            doAfterExecute();
                            return true;
                        }
                    }
                }
            }
        }
        doAfterExecute();
        return false;
    }

    private void doAfterExecute(){}
}
