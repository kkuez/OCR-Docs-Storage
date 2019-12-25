package com.misc;

public class ProgressManager {
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
