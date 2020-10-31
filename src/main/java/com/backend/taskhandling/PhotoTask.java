package com.backend.taskhandling;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

import com.backend.BackendFacade;
import com.backend.taskhandling.strategies.SimpleCalendarOneTimeStrategy;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;
import com.objectTemplates.User;

public class PhotoTask extends Task {

    private Future photoFuture;

    User user;

    public PhotoTask(User user, Bot bot, Future future, BackendFacade facade) {
        super(bot);
        this.user = user;
        this.photoFuture = future;
        setExecutionStrategy(new SimpleCalendarOneTimeStrategy(this,
                LocalDateTime.now().plusMinutes(3).withSecond(0).withNano(0), facade));
    }

    @Override
    public boolean perform() {
        boolean successCanceling = photoFuture.cancel(true);
        if (successCanceling) {
            getBot().sendSimpleMsg("Abgebrochen, 3 Minuten vorbei :( Versuchs nochmal!", user.getId(),
                    KeyboardFactory.KeyBoardType.NoButtons, false, null);
            user.setBusy(false);
            user.setProcess(null);
        }
        return successCanceling;
    }
}
