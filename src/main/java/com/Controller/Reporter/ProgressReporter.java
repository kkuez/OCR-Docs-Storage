package com.Controller.Reporter;

import com.Misc.ProgressManager;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ProgressReporter extends Reporter {

    ProgressManager progressManager= new ProgressManager();

    void setTotalSteps(int steps, Update updateOrNull);

    void addStep(Update updateOrNull);

    void setStep(int step, Update updateOrNull);
}
