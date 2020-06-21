package com.backend.taskHandling;

import com.backend.BackendFacade;
import com.backend.taskHandling.strategies.SimpleCalendarOneTimeStrategy;
import com.objectTemplates.User;
import com.bot.telegram.Bot;
import com.bot.telegram.KeyboardFactory;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

public class PhotoTask extends Task {

    private Future photoFuture;

    User user;

    public PhotoTask(User user, Bot bot, Future future, BackendFacade facade) {
        super(bot);
        this.user = user;
        this.photoFuture = future;
        setExecutionStrategy(new SimpleCalendarOneTimeStrategy(this, LocalDateTime.now().plusMinutes(3).withSecond(0).withNano(0), facade));
    }

    @Override
    public boolean perform(){
        boolean successCanceling = photoFuture.cancel(true);
        if(successCanceling){
            getBot().sendSimpleMsg("Abgebrochen, 3 Minuten vorbei :( Versuchs nochmal!", user.getId(), KeyboardFactory.KeyBoardType.NoButtons, false);
            user.setBusy(false);
            user.setProcess(null);
        }
        return successCanceling;
    }
}
