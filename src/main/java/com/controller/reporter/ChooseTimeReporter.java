package com.controller.reporter;

import java.time.LocalDate;

public interface ChooseTimeReporter extends Reporter {

    void submitTimes(LocalDate beginDate, LocalDate endDate);

    enum Time{
        lastYear, lastMonth, lastSixMonth, specificTime
    }
}
