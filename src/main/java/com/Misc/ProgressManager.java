package com.Misc;

public class ProgressManager {
    private int totalSteps;

    private int currentStep;

    public ProgressManager(int totalSteps){
        this.totalSteps = totalSteps;
    }

    //GETTER SETTER

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }
}
