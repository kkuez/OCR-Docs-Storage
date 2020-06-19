package com.gui;

import com.Main;
import org.apache.log4j.Logger;

public class ProgressManager {

    private static Logger logger = Main.getLogger();

    private int totalSteps;

    private int currentStep;

    public void addStep(){
        currentStep += currentStep + 1;
    }

    public double getCurrentProgress(){
        return (double)currentStep / (double)totalSteps;
    }

    //GETTER SETTER

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        currentStep = 0;
        this.totalSteps = totalSteps;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }
}
