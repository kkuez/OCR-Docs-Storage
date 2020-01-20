package com.misc.taskHandling;

import com.misc.taskHandling.strategies.OneTimeTaskStrategy;
import com.misc.taskHandling.strategies.SimpleCalendarOneTimeStrategy;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.telegram.KeyboardFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

public class PhotoTask extends Task {


    Future photoFuture = null;

    public PhotoTask(User user, Bot bot, Future future) {
        super(user, bot);
        this.photoFuture = future;
        setTaskStrategy(new SimpleCalendarOneTimeStrategy(this, LocalDateTime.now().plusMinutes(3).withSecond(0).withNano(0)));
    }

    @Override
    public boolean perform(){
        boolean successCanceling = photoFuture.cancel(true);
        if(successCanceling){
            getBot().sendSimpleMsg("Abgebrochen, 3 Minuten vorbei :( Versuchs nochmal!", getUserList().get(0).getId(), KeyboardFactory.KeyBoardType.NoButtons, false);
            getUserList().get(0).setBusy(false);
            getUserList().get(0).setProcess(null);
        }
        return successCanceling;
    }
}
