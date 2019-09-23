package com.Controller.Reporter;

public interface ProgressReporter extends Reporter {

    void setSteps(int steps);

    void addStep();

    void setStep(int step);
}
