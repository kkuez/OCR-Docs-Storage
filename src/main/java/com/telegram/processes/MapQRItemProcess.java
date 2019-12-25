package com.telegram.processes;

import com.controller.reporter.ProgressReporter;
import com.objectTemplates.User;
import com.telegram.Bot;
import com.telegram.KeyboardFactory;
import com.utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

public class MapQRItemProcess extends Process {
    int itemNumberToMap = 0;

    Step currentStep = null;

    public MapQRItemProcess(Bot bot, ProgressReporter reporter, Update update) {
        super(reporter);
        setBot(bot);

        Message message = bot.sendMsg("Welches Item willst du mappen?", update, KeyboardFactory.KeyBoardType.QRItems, true, true);
        getSentMessages().add(message);
        currentStep = Step.chooseNumber;
    }

    @Override
    public void performNextStep(String arg, Update update, Map<Integer, User> allowedUsersMap) throws TelegramApiException {
        Message message = null;
        switch (currentStep){
            case chooseNumber:
                itemNumberToMap = Integer.parseInt(update.getCallbackQuery().getData());
                message = getBot().simpleEditMessage("Und was..?", update, KeyboardFactory.KeyBoardType.Abort);
                getBot().sendAnswerCallbackQuery("Und was?", false, update.getCallbackQuery());
                getSentMessages().add(message);
                currentStep = Step.nameItem;
                break;
            case nameItem:
                DBUtil.updateQRItem(itemNumberToMap, update.getMessage().getText());
                message = getBot().sendMsg("Ok :)", update, KeyboardFactory.KeyBoardType.NoButtons, false, false);
                getSentMessages().add(message);
                close();
                break;
        }
    }

    @Override
    public String getProcessName() {
        return null;
    }

    @Override
    public String getCommandIfPossible(Update update) {
        return null;
    }

    private enum Step{
        chooseNumber, nameItem
    }
}
