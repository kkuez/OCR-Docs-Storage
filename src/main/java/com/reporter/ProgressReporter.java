package com.reporter;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ProgressReporter extends Reporter {

    void setTotalSteps(int steps, Update updateOrNull);

    void addStep(Update updateOrNull);

    void setStep(int step, Update updateOrNull);
}
