package com.gui.controller.reporter;

import java.time.LocalDate;

public interface ChooseTimeReporter extends Reporter {

    public void submitTimes(LocalDate beginDate, LocalDate endDate);

    enum Time{
        lastYear, lastMonth, lastSixMonth, specificTime
    }
}
