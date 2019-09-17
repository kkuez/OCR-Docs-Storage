package com.Misc.Processes;

import com.Misc.KeyboardFactory;
import com.ObjectTemplates.Document;
import com.Telegram.Bot;
import com.Utils.DBUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class GetPicsProcess extends Process {

    String searchTerm;
    public GetPicsProcess(Bot bot, Update update){
        String input = update.getMessage().getText();
        String searchTerm = input.substring(input.indexOf(" ") + 1);
        this.searchTerm = searchTerm;
        setBot(bot);
        performNextStep(searchTerm, update);
    }
    @Override
    public void performNextStep(String arg, Update update) {
        getBot().setBusy(true);
        List<Document> listOfDocs = DBUtil.getFilesForSearchTerm(searchTerm);
        listOfDocs.forEach(document -> getBot().sendPhotoFromURL(update, document.getOriginFile().getAbsolutePath(), "", null));
        getBot().setBusy(false);
        getBot().process = null;
    }
}
