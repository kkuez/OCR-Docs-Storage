package com.backend.taskhandling;

import static com.backend.taskhandling.strategies.StrategyType.SIMPLECALENDAR_ONETIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.backend.taskhandling.strategies.StrategyType;

public class TaskFactoryTest {

    @Test
    void getStrategyTypeOrNullTest() {
        final StrategyType resultStrategy = TaskFactory.getStrategyTypeOrNull("SIMPLECALENDAR_ONETIME");
        assertEquals(SIMPLECALENDAR_ONETIME, resultStrategy);
        assertNull(TaskFactory.getStrategyTypeOrNull("Quark"));
    }

}
