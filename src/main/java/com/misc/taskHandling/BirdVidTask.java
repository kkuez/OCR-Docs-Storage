package com.misc.taskHandling;

import com.Main;
import com.ObjectHub;
import com.objectTemplates.User;
import com.telegram.Bot;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BirdVidTask extends Task {

    private Logger logger = Main.getLogger();

    public BirdVidTask(List<User> userList, Bot bot, String actionName) {
        super(userList, bot, actionName);
    }

    @Override
    public boolean perform() {
        File birdVidFolder = new File(ObjectHub.getInstance().getProperties().getProperty("birdVidFolder"));
        if(!birdVidFolder.exists()){
            birdVidFolder.mkdir();
        }

        File sentbirdVidFolder = new File(birdVidFolder, "sent");
        if(!sentbirdVidFolder.exists()){
            sentbirdVidFolder.mkdir();
        }

        Collection<File> vids = FileUtils.listFiles(birdVidFolder, new String[]{"mp4"}, false);
        for(File vid: vids){
            ObjectHub.getInstance().getAllowedUsersMap().values().forEach(user -> {
                getBot().sendVideoFromURL(user, vid.getAbsolutePath(), "(:");
                logger.info(vid.getName() + " sent to " + user.getName());
        });
            try {
                FileUtils.copyFile(vid, new File(sentbirdVidFolder, vid.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            vid.delete();
        }
        return true;
    }


}
